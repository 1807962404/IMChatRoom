package edu.hniu.imchatroom.controller;

import edu.hniu.imchatroom.model.bean.Group;
import edu.hniu.imchatroom.model.bean.GroupUser;
import edu.hniu.imchatroom.model.bean.ResultVO;
import edu.hniu.imchatroom.model.bean.User;
import edu.hniu.imchatroom.model.enums.StatusCodeEnum;
import edu.hniu.imchatroom.service.GroupService;
import edu.hniu.imchatroom.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static edu.hniu.imchatroom.util.VariableUtil.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class GroupController {

    private GroupService groupService;
    @Autowired
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * 获取我加入的所有群组列表
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/get-my-entered-groups")
    public List<Group> doGetMyEnteredGroups(HttpServletRequest request) {
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);

        // 1、获取本人加入的所有群聊列表
        List<Group> myEnteredGroups = groupService.doGetMyEnteredGroups(thisUser.getUId());
        log.info("My Entered Groups: {}", myEnteredGroups);

        for (Group myEnteredGroup : myEnteredGroups) {
            // 2、获取我加入的群组中所有的成员
            List<GroupUser> groupUsers = groupService.doGetGroupsUsersById(myEnteredGroup.getGId(), null);

            // 3、将群组成员设置到该群组中
            myEnteredGroup.setMembers(groupUsers);
        }

        return myEnteredGroups;
    }

    /**
     * 获取我创建的群组列表
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/get-my-created-groups")
    public List<Group> doGetMyCreatedGroups(HttpServletRequest request) {
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        List<Group> groups = groupService.doGetMyGroups(thisUser.getUId());
        log.info("用户 {} 创建的群组信息：{}", thisUser.getNickname(), groups);
        return groups;
    }

    /**
     * 用户新建群组
     * @param data
     * @param request
     * @return
     */
    @ResponseBody
    @PostMapping("/add-group")
    public ResultVO<Group> doAddGroup(String data, HttpServletRequest request) {
        ResultVO<Group> resultVO = new ResultVO<>();
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        Group newGroup = new Group();

        // 1、检查群聊名称是否为空
        if (StringUtil.isEmpty(data)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("新建群聊名称不能为空！");
            log.warn("新建群聊名称不能为空！");
            return resultVO;
        }

        // 2、为新建群聊设置初始值
        // 2.1、设置群聊名称
        newGroup.setGName(data);
        // 2.2、设置群主
        newGroup.setHostUser(thisUser);

        // 3、新增群组信息
        int result = groupService.doAddGroup(newGroup);
        if (2 != result) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("用户： " + thisUser.getNickname() + " 新建群聊失败！");
            log.warn("用户：{} 新建群聊失败！", thisUser.getNickname());

        } else {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            // 需设置默认的群组内成员就是群主
            GroupUser defaultGroupUser = new GroupUser();
            defaultGroupUser.setMember(thisUser);
            newGroup.setMembers(Arrays.asList(defaultGroupUser));

            resultVO.setData(newGroup);
            resultVO.setMsg("用户： " + thisUser.getNickname() + " 新建群聊：" + newGroup.getGName() + " 成功！");
            log.info("用户：{} 新建群聊：{} 成功！", thisUser.getNickname(), newGroup.getGName());
            log.info("新建群聊：{} 信息如下：{}", newGroup.getGName(), newGroup);
        }

        return resultVO;
    }

    /**
     * 用户查询指定群组
     * @param data
     * @return
     */
    @ResponseBody
    @PostMapping("/find-group")
    public ResultVO<List<Group>> doFindGroup(String data) {

        ResultVO<List<Group>> resultVO = new ResultVO<>();

        log.info("搜索的群聊信息为：{}", data);
        // 1、检查搜索内容是否为空
        if (StringUtil.isEmpty(data)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("搜索的群聊名称不能为空！");
            log.warn("搜索的群聊名称不能为空！");
            return resultVO;
        }

        // 2、模糊查询相应群聊信息（gCode, gName）
        List<Group> findGroups = groupService.doGetGroupsByFuzzyQuery(data);
        if (null == findGroups || 0 == findGroups.size()) {
            resultVO.setCode(RESPONSE_WARNING_CODE);
            resultVO.setMsg("未搜索到关键词为：'" + data + "' 的群聊信息，请查证后再试！");
            log.warn("未搜索到关键词为：'{}' 的群聊信息，请查证后再试！", data);

        } else {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setData(findGroups);
            resultVO.setMsg("已搜索出关键词为：'" + data + "' 的群聊信息！");
            log.info("已搜索出关键词为：'{}' 的群聊信息：{}", data, findGroups);
        }

        return resultVO;
    }

    /**
     * 用户通过群组的gCode加入群组
     * @param gCode
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/enter-group/{gCode}")
    public ResultVO doEnterGroup(@PathVariable("gCode") String gCode,
                               HttpServletRequest request) {

        log.info("加入群聊的code唯一码为：{}", gCode);
        ResultVO resultVO = new ResultVO<>();

        // 1、检查传入的gCode是否为空
        if (StringUtil.isEmpty(gCode)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("加入群聊的唯一码gCode不能为空！");
            log.info("加入群聊的唯一码gCode不能为空！");
            return resultVO;
        }

        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);

        // 2、判断群组是否存在
        Group enterGroup = groupService.doGetGroupByGCode(gCode);
        if (null == enterGroup) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("未找到群码gCode为：" + gCode + " 的群组信息！");
            log.info("未找到群码gCode为：{} 的群组信息！", gCode);
            return resultVO;
        }

        // 3、检查此用户是否存在于该群组中
        GroupUser userIsInGroup = groupService.doCheckUserIsInGroup(enterGroup.getGId(), thisUser.getUId());
        if (null != userIsInGroup) {
            if (userIsInGroup.getGuStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.NORMAL))) {
                // 该用户仍在该群组中
                resultVO.setCode(RESPONSE_WARNING_CODE);
                resultVO.setMsg("您已在：" + userIsInGroup.getGroup().getGName() + " 群组中！");
                log.warn("您已在：{} 群组中，无法发送入群申请！", userIsInGroup.getGroup().getGName());
                return resultVO;

            } else if (userIsInGroup.getGuStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.CONFIRMING))) {
                // 该用户已发送过入群申请
                resultVO.setCode(RESPONSE_WARNING_CODE);
                resultVO.setMsg("您已向群组：" + userIsInGroup.getGroup().getGName() + " 发送过入群申请，请勿重复发送请求！");
                log.warn("您已向群组：{} 发送过入群申请，请勿重复发送请求！", userIsInGroup.getGroup().getGName());
                return resultVO;
            }
        }

        // 4、用户发送入群申请给此群群主
        int result = groupService.doAddGroupUser(enterGroup, thisUser, StatusCodeEnum.getStatusCode(StatusCodeEnum.CONFIRMING));
        if (1 == result) {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setMsg("已成功向群组：" + enterGroup.getGName() + " 发送入群申请，请敬候佳音！");
            log.info("已成功向群组：{} 发送入群申请，请敬候佳音！", enterGroup.getGName());

        } else {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("无法向群组：" + enterGroup.getGName() + " 发送入群申请，请稍后再试！");
            log.warn("无法向群组：{} 发送入群申请，请稍后再试！", enterGroup.getGName());
        }
        return resultVO;
    }

    /**
     * 查询群通知消息（查看哪些用户申请加入本人管理的哪些群聊）
     * @return
     */
    @ResponseBody
    @GetMapping("/find-group-notifications")
    public ResultVO<List<GroupUser>> doFindGroupNotifications(HttpServletRequest request) {

        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        ResultVO<List<GroupUser>> resultVO = new ResultVO<>();

        // 1、先查询我的所有群组信息
        List<Group> groups = groupService.doGetMyGroups(thisUser.getUId());

        List<GroupUser> groupsUsersToUse = new ArrayList<>();
        // 2、循环遍历我的群组，查询是否有用户申请加入我创建的群组
        for (Group group : groups) {
            List<GroupUser> groupUsers = groupService.doGetGroupsUsersById(group.getGId(), null);
            if (null != groupUsers && groupUsers.size() > 0) {
                for (GroupUser groupUser : groupUsers) {
                    // 判断该用户是否已在群聊中，或正待本人确认同意其加入群聊
                    if (groupUser.getGuStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.NORMAL)) ||
                            groupUser.getGuStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.CONFIRMING))) {
                        groupsUsersToUse.add(groupUser);
                    }
                }
            }
        }

        resultVO.setCode(RESPONSE_SUCCESS_CODE);
        resultVO.setData(groupsUsersToUse);
        log.info("已查询出用户：{} 名下所有群聊以及群聊成员的信息：{}", thisUser.getNickname(), groupsUsersToUse);

        return resultVO;
    }

    /**
     * 同意用户进入本人的群组
     * @param gId
     * @param uId
     * @return
     */
    @ResponseBody
    @GetMapping("/agree-enter-group/{gId}/{uId}")
    public ResultVO doAgreeUserEnterMyGroup(@PathVariable("gId") String gId,
                                          @PathVariable("uId") String uId,
                                          HttpServletRequest request
    ) {

        ResultVO resultVO = new ResultVO<>();
        // 1、检查内容是否为空
        if (StringUtil.isEmpty(gId) || StringUtil.isEmpty(uId)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("内容为空，无法同意此用户进入您的群组！");
            log.warn("内容为空，无法同意此用户进入您的群组！");
            return resultVO;
        }

        // 2、查询出对应的GroupUser记录
        GroupUser groupUser = groupService.doCheckUserIsInGroup(Integer.valueOf(gId), Integer.valueOf(uId));
        if (null == groupUser) {
            resultVO.setCode(RESPONSE_WARNING_CODE);
            resultVO.setMsg("未查到该用户的入群申请，同意失败！");
            log.warn("未查到该用户的入群申请，同意失败！");
            return resultVO;
        }

        log.info("Group user: " + groupUser);

        // 3、检查本人是否为该群群主
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        if (!thisUser.equals(groupUser.getGroup().getHostUser())) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("您不是群聊：" + groupUser.getGroup().getGName() + " 的群主，无法同意此请求！");
            log.warn("您不是群聊：{} 的群主，无法同意此请求！", groupUser.getGroup().getGName());
            return resultVO;
        }

        // 4、同意用户入群
        int result = groupService.doUpdateUserInGroup(groupUser, true);
        if (1 == result) {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setMsg("已同意用户：" + groupUser.getMember().getNickname() +
                    " 进入您的群聊：" + groupUser.getGroup().getGName() + "！");
            log.info("已同意用户：{} 进入您的群聊：{}！", groupUser.getMember().getNickname(), groupUser.getGroup().getGName());

        } else {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("无法同意此用户进入您的群聊，请稍后再试！");
            log.warn("无法同意此用户进入您的群聊，请稍后再试！");
        }

        return resultVO;
    }

    /**
     * 将用户踢出群组
     * @param gId
     * @param uId
     * @return
     */
    @ResponseBody
    @GetMapping("/drop-member-from-group/{gId}/{uId}")
    public ResultVO doDropMemberFromGroup(@PathVariable("gId") String gId,
                                        @PathVariable("uId") String uId,
                                        HttpServletRequest request
    ) {

        ResultVO resultVO = new ResultVO<>();
        // 1、检查内容是否为空
        if (StringUtil.isEmpty(gId) || StringUtil.isEmpty(uId)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("内容为空，无法踢出群组！");
            log.warn("内容为空，无法踢出群组！");
            return resultVO;
        }

        // 2、查询出对应的GroupUser记录
        GroupUser groupUser = groupService.doCheckUserIsInGroup(Integer.valueOf(gId), Integer.valueOf(uId));
        if (null == groupUser) {
            resultVO.setCode(RESPONSE_WARNING_CODE);
            resultVO.setMsg("未查到该用户的入群记录，无法踢出群聊！");
            log.warn("未查到该用户的入群申请，无法踢出群聊！");
            return resultVO;
        }

        log.info("Group user: " + groupUser);

        // 3、检查本人是否为该群群主
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        if (!thisUser.equals(groupUser.getGroup().getHostUser())) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("您不是群聊：" + groupUser.getGroup().getGName() + " 的群主，无法将其踢出群聊！");
            log.warn("您不是群聊：{} 的群主，无法将其踢出群聊！", groupUser.getGroup().getGName());
            return resultVO;
        }

        // 4、踢出用户
        int result = groupService.doUpdateUserInGroup(groupUser, false);
        if (1 == result) {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setMsg("已将用户：" + groupUser.getMember().getNickname() +
                    " 踢出您的群聊：" + groupUser.getGroup().getGName() + "！");
            log.info("已将用户：{} 踢出您的群聊：{}！", groupUser.getMember().getNickname(), groupUser.getGroup().getGName());

        } else {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("无法将此用户踢出您的群聊，请稍后再试！");
            log.warn("无法将此用户踢出您的群聊，请稍后再试！");
        }

        return resultVO;
    }

    /**
     * 解散群聊
     * @param gCode
     * @return
     */
    @ResponseBody
    @GetMapping("/dissolve-group/{gCode}")
    public ResultVO doDissolveGroup(@PathVariable("gCode") String gCode,
                                  HttpServletRequest request) {

        ResultVO resultVO = new ResultVO<>();
        // 1、检查内容是否为空
        if (StringUtil.isEmpty(gCode)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("群组唯一码不能为空！");
            log.warn("群组唯一码不能为空！");
            return resultVO;
        }

        // 2、查询gCode对应的群组信息
        Group dissolveGroup = groupService.doGetGroupByGCode(gCode);
        if (null == dissolveGroup) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("未找到gCode为：" + gCode + " 的群组信息！");
            log.warn("未找到gCode为：{} 的群组信息！", gCode);
            return resultVO;
        }

        log.info("Dissolve Group: {}", dissolveGroup);

        // 3、检查本人是否为该群群主
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        if (!thisUser.equals(dissolveGroup.getHostUser())) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("您不是群聊：" + dissolveGroup.getGName() + " 的群主，无法解散群聊！");
            log.warn("您不是群聊：{} 的群主，无法解散群聊！", dissolveGroup.getGName());
            return resultVO;
        }

        // 4、将该群中所有的用户都先踢掉
        int result = 0;
        // 拿到该群所有用户的信息
        List<GroupUser> groupUsers = groupService.doGetGroupsUsersById(dissolveGroup.getGId(), null);
        for (GroupUser groupUser : groupUsers) {
            // 依次将用户踢出该群聊
            result += groupService.doUpdateUserInGroup(groupUser, false);
        }

        // 5、解散此群聊
        dissolveGroup.setModifiedTime(new Date(System.currentTimeMillis()));
        dissolveGroup.setDisplayStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.ABNORMAL));
        result += groupService.doUpdateGroup(dissolveGroup);

        if (2 <= result) {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setMsg("已成功解散群组：" + dissolveGroup.getGName() + "！");
            log.info("已成功解散群组：{}！", dissolveGroup.getGName());

        } else {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("未能解散群组：" + dissolveGroup.getGName() + "，请稍后再试！");
            log.warn("未能解散群组：{}，请稍后再试！", dissolveGroup.getGName());
        }

        return resultVO;
    }

    /**
     * 退出群聊
     * @param gCode
     * @return
     */
    @ResponseBody
    @GetMapping("/exit-group/{gCode}")
    public ResultVO doExitGroup(@PathVariable("gCode") String gCode,
                              HttpServletRequest request) {

        ResultVO resultVO = new ResultVO<>();
        // 1、检查内容是否为空
        if (StringUtil.isEmpty(gCode)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("群组唯一码不能为空！");
            log.warn("群组唯一码不能为空！");
            return resultVO;
        }

        // 2、查询gCode对应的群组信息
        Group exitGroup = groupService.doGetGroupByGCode(gCode);
        if (null == exitGroup) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("未找到gCode为：" + gCode + " 的群组信息！");
            log.warn("未找到gCode为：{} 的群组信息！", gCode);
            return resultVO;
        }

        log.info("Exit Group: {}", exitGroup);

        // 3、检查本人是否为该群群主
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        if (thisUser.equals(exitGroup.getHostUser())) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("您是群聊：" + exitGroup.getGName() + " 的群主，无法退出群聊！");
            log.warn("您是群聊：{} 的群主，无法退出群聊，请使用解散群聊功能！", exitGroup.getGName());
            return resultVO;
        }

        // 5、查询出本人的入群记录
        List<GroupUser> groupUsers = groupService.doGetGroupsUsersById(exitGroup.getGId(), thisUser.getUId());
        if (null == groupUsers || groupUsers.size() == 0) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("您不是群聊：" + exitGroup.getGName() + " 的成员，无法退出群聊！");
            log.warn("您不是群聊：{} 的成员，无法退出群聊，请使用解散群聊功能！", exitGroup.getGName());
            return resultVO;
        }

        // 6、退出此群聊
        int result = groupService.doUpdateUserInGroup(groupUsers.get(0), false);

        if (1 == result) {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setMsg("已成功退出群组：" + exitGroup.getGName() + "！");
            log.info("已成功退出群组：{}！", exitGroup.getGName());

        } else {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("未能退出群组：" + exitGroup.getGName() + "，请稍后再试！");
            log.warn("未能退出群组：{}，请稍后再试！", exitGroup.getGName());
        }

        return resultVO;
    }
}
