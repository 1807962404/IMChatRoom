package edu.hniu.imchatroom.controller;

import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.bean.messages.StatusCode;
import edu.hniu.imchatroom.model.bean.response.Result;
import edu.hniu.imchatroom.model.bean.response.ResultVO;
import edu.hniu.imchatroom.service.GroupService;
import edu.hniu.imchatroom.service.UserService;
import edu.hniu.imchatroom.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static edu.hniu.imchatroom.util.VariableUtil.SIGNINED_USER;

@Slf4j
@RestController
@RequestMapping("/user")
public class GroupController {

    private GroupService groupService;
    private UserService userService;
    @Autowired
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取我加入的所有群组列表
     * @param request
     * @return
     */
    @GetMapping("/get-my-entered-groups")
    public List<Group> doGetMyEnteredGroups(HttpServletRequest request) {

        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);

        // 1、获取本人加入的所有群聊列表
        List<Group> myEnteredGroups = groupService.doGetMyEnteredGroups(thisUser.getUId());
        log.info("My Entered Groups: {}", myEnteredGroups);

        return myEnteredGroups;
    }

    /**
     * 获取我创建的群组列表
     * @param request
     * @return
     */
    @GetMapping("/get-my-created-groups")
    public List<Group> doGetMyCreatedGroups(HttpServletRequest request) {

        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        List<Group> groups = groupService.doGetMyCreatedGroups(thisUser.getUId());
        log.info("用户 {} 创建的群组信息：{}", thisUser.getNickname(), groups);

        return groups;
    }

    /**
     * 用户新建群组
     * @param data
     * @param request
     * @return
     */
    @PostMapping("/add-group")
    public ResultVO<Group> doAddGroup(String data, HttpServletRequest request) {

        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        Group newGroup = new Group();

        // 1、检查群聊名称是否为空
        if (StringUtil.isEmpty(data)) {
            log.warn("新建群聊名称不能为空！");
            return Result.warn("新建群聊名称不能为空！");
        }

        // 2、为新建群聊设置初始值
        // 2.1、设置群聊名称
        newGroup.setGName(data);
        // 2.2、设置群主
        newGroup.setHostUser(thisUser);

        // 3、新增群组信息
        int result = groupService.doAddGroup(newGroup);
        if (2 != result) {
            log.warn("用户：{} 新建群聊失败！", thisUser.getNickname());
            return Result.failed("用户： " + thisUser.getNickname() + " 新建群聊失败！");

        }

        // 需设置默认的群组内成员就是群主
        GroupUser defaultGroupHostUser = new GroupUser();
        defaultGroupHostUser.setMember(thisUser);
        defaultGroupHostUser.setGuStatus(StatusCode.getInGroupStatusCode());
        newGroup.setMembers(Arrays.asList(defaultGroupHostUser));

        log.info("用户：{} 新建群聊：{} 成功！", thisUser.getNickname(), newGroup.getGName());
        log.info("新建群聊：{} 信息如下：{}", newGroup.getGName(), newGroup);
        return Result.ok("用户： " + thisUser.getNickname() + " 新建群聊：" + newGroup.getGName() + " 成功！", newGroup);
    }

    /**
     * 用户查询指定群组
     * @param data
     * @return
     */
    @PostMapping("/find-group")
    public ResultVO<List<Group>> doFindGroup(String data) {

        log.info("搜索的群聊信息为：{}", data);
        // 1、检查搜索内容是否为空
        if (StringUtil.isEmpty(data)) {
            log.warn("搜索的群聊名称不能为空！");
            return Result.warn("搜索的群聊名称不能为空！");
        }

        // 2、模糊查询相应群聊信息（gCode, gName）
        List<Group> findGroups = groupService.doGetGroupsByFuzzyQuery(data);
        if (findGroups.isEmpty()) {
            log.warn("未搜索到关键词为：'{}' 的群聊信息，请查证后再试！", data);
            return Result.warn("未搜索到关键词为：'" + data + "' 的群聊信息，请查证后再试！");

        }

        log.info("已搜索出关键词为：'{}' 的群聊信息：{}", data, findGroups);
        return Result.ok("已搜索出关键词为：'" + data + "' 的群聊信息！", findGroups);
    }

    /**
     * 用户通过群组的gCode加入群组
     * @param gCode
     * @param request
     * @return
     */
    @GetMapping("/enter-group/{gCode}")
    public ResultVO doEnterGroup(@PathVariable("gCode") String gCode,
                               HttpServletRequest request) {

        log.info("加入群聊的code唯一码为：{}", gCode);

        // 1、检查传入的gCode是否为空
        if (StringUtil.isEmpty(gCode)) {
            log.info("加入群聊的唯一码gCode不能为空！");
            return Result.failed("加入群聊的唯一码gCode不能为空！");
        }

        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);

        // 2、判断群组是否存在
        Group enterGroup = groupService.doGetGroupByGCode(gCode);
        if (null == enterGroup) {
            log.info("未找到群码gCode为：{} 的群组信息！", gCode);
            return Result.failed("未找到群码gCode为：" + gCode + " 的群组信息！");
        }

        // 3、检查此用户是否存在于该群组中
        GroupUser userIsInGroup = groupService.doCheckUserIsInGroup(enterGroup.getGId(), thisUser.getUId());
        if (null != userIsInGroup) {
            if (userIsInGroup.getGuStatus().equals(StatusCode.getInGroupStatusCode())) {
                // 该用户仍在该群组中
                log.warn("您已在：{} 群组中，无法发送入群申请！", userIsInGroup.getGroup().getGName());
                return Result.warn("您已在：" + userIsInGroup.getGroup().getGName() + " 群组中！");

            } else if (userIsInGroup.getGuStatus().equals(StatusCode.getConfirmingStatusCode())) {
                // 该用户已发送过入群申请
                log.warn("您已向群组：{} 发送过入群申请，请勿重复发送请求！", userIsInGroup.getGroup().getGName());
                return Result.warn("您已向群组：" + userIsInGroup.getGroup().getGName() + " 发送过入群申请，请勿重复发送请求！");
            }
        }

        // 4、用户发送入群申请给此群群主
        int result = groupService.doAddGroupUser(enterGroup, thisUser, StatusCode.getConfirmingStatusCode());
        if (1 != result) {
            log.warn("无法向群组：{} 发送入群申请，请稍后再试！", enterGroup.getGName());
            return Result.failed("无法向群组：" + enterGroup.getGName() + " 发送入群申请，请稍后再试！");
        }

        // 5、获取此用户发送的入群申请信息
        GroupUser applyGroupUser = groupService.doCheckUserIsInGroup(enterGroup.getGId(), thisUser.getUId());
        log.info("已成功向群组：{} 发送入群申请，请敬候佳音！", enterGroup.getGName());
        return Result.ok("已成功向群组：" + enterGroup.getGName() + " 发送入群申请，请敬候佳音！", applyGroupUser);
    }

    /**
     * 查询群通知消息（查看哪些用户申请加入本人管理的哪些群聊）
     * @return
     */
    @GetMapping("/find-group-notifications")
    public ResultVO<List<GroupUser>> doFindGroupNotifications(HttpServletRequest request) {

        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);

        // 查询我创建的群组 和 我加入的群组 下的用户信息
        // 1、查询关于我的所有群组用户信息（包含我创建的群聊下的所有用户信息，以及我加入、申请加入的群聊下的所有用户信息）
        List<GroupUser> groupUsersToUse = new ArrayList<>();

        // 我的所有群组用户信息
        List<GroupUser> myGroupUsers = groupService.doGetGroupUserByIdAndGuStatus(thisUser.getUId(), null);

        if (null != myGroupUsers && !myGroupUsers.isEmpty()) {
            Iterator<GroupUser> groupUserIterator = myGroupUsers.iterator();
            while (groupUserIterator.hasNext()) {
                GroupUser groupUser = groupUserIterator.next();
                Group group = groupUser.getGroup();
                // 获取每个群组中所有的群组成员信息
                List<GroupUser> groupUsers = groupService.doGetGroupUserById(group.getGId(), null);
                if (null != groupUsers && !groupUsers.isEmpty()) {
                    Iterator<GroupUser> iterator = groupUsers.iterator();
                    while (iterator.hasNext()) {
                        GroupUser tempGroupUser = iterator.next();
                        // 2、排除群主
                        if (group.getHostUser().equals(tempGroupUser.getMember()))
                            iterator.remove();

                            // 3、排除已不在此群组的用户
                        else if (groupUser.getGuStatus().equals(StatusCode.getNotInGroupStatusCode()))
                            iterator.remove();
                    }
                    groupUsersToUse.addAll(groupUsers);
                }
            }
        }

        log.info("已查询出关于用户：{} 的所有群组用户信息（包含我创建的群聊下的所有用户信息，" +
                "以及我加入、申请加入的群聊下的所有用户信息）：{}", thisUser.getNickname(), groupUsersToUse);
        return Result.ok(groupUsersToUse);
    }

    /**
     * 同意用户进入本人的群组
     * @param gId
     * @param uId
     * @return
     */
    @GetMapping("/agree-enter-group/{gId}/{uId}")
    public ResultVO doAgreeUserEnterMyGroup(@PathVariable("gId") String gId,
                                          @PathVariable("uId") String uId,
                                          HttpServletRequest request
    ) {

        // 1、检查内容是否为空
        if (StringUtil.isEmpty(gId) || StringUtil.isEmpty(uId)) {
            log.warn("内容为空，无法同意此用户进入您的群组！");
            return Result.failed("内容为空，无法同意此用户进入您的群组！");
        }

        // 2、检查是否存在该uId的用户
        User applyUser = userService.doGetUserById(Long.valueOf(uId));
        if (null == applyUser) {
            log.warn("uId为：{} 的用户不存在，同意失败！", uId);
            return Result.failed("uId为：" + uId + " 的用户不存在，同意失败！");
        }

        // 3、查询出对应的GroupUser记录
        GroupUser groupUser = groupService.doCheckUserIsInGroup(Long.valueOf(gId), Long.valueOf(uId));
        if (null == groupUser) {
            log.warn("未查到用户：{} 的入群申请，同意失败！", applyUser.getNickname());
            return Result.failed("未查到用户：" + applyUser.getNickname() + " 的入群申请，同意失败！");
        }

        log.info("Group user: " + groupUser);

        // 4、检查本人是否为该群群主
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        if (!thisUser.equals(groupUser.getGroup().getHostUser())) {
            log.warn("您不是群聊：{} 的群主，没有权限同意此请求！", groupUser.getGroup().getGName());
            return Result.failed("您不是群聊：" + groupUser.getGroup().getGName() + " 的群主，没有权限同意此请求！");
        }

        // 5、同意用户入群
        int result = groupService.doUpdateUserInGroup(groupUser, true);
        if (1 != result) {
            log.warn("无法同意此用户进入您的群聊，请稍后再试！");
            return Result.failed("无法同意此用户进入您的群聊，请稍后再试！");
        }

        log.info("已同意用户：{} 进入您的群聊：{}！", groupUser.getMember().getNickname(), groupUser.getGroup().getGName());
        return Result.ok("已同意用户：" + groupUser.getMember().getNickname() +
                " 进入您的群聊：" + groupUser.getGroup().getGName() + "！", groupUser);
    }

    /**
     * 将用户移出群组
     * @param gId
     * @param uId
     * @return
     */
    @GetMapping("/drop-member-from-group/{gId}/{uId}")
    public ResultVO doDropMemberFromGroup(@PathVariable("gId") String gId,
                                        @PathVariable("uId") String uId,
                                        HttpServletRequest request
    ) {

        // 1、检查内容是否为空
        if (StringUtil.isEmpty(gId) || StringUtil.isEmpty(uId)) {
            log.warn("移出群友的uId为空，无法移出群组！");
            return Result.failed("移出群友的uId为空，无法移出群组！");
        }

        // 2、检查是否存在该uId的用户
        User dropedUser = userService.doGetUserById(Long.valueOf(uId));
        if (null == dropedUser) {
            log.warn("uId为：{} 的用户不存在，移出用户失败！", uId);
            return Result.failed("uId为：" + uId + " 的用户不存在，移出用户失败！");
        }

        // 3、查询出对应的GroupUser记录
        GroupUser groupUser = groupService.doCheckUserIsInGroup(Long.valueOf(gId), Long.valueOf(uId));
        if (null == groupUser) {
            log.warn("未在群组：{} 查询到群友：{} 的信息，无法移出群聊！",
                    groupUser.getGroup().getGName(), groupUser.getMember().getNickname());
            return Result.warn("未在群组：" + groupUser.getGroup().getGName() +
                    " 查询到群友：" + groupUser.getMember().getNickname() + " 的信息，无法移出群聊！");
        }

        log.info("Group user: " + groupUser);

        // 3、检查本人是否为该群群主
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        if (!thisUser.equals(groupUser.getGroup().getHostUser())) {
            log.warn("您不是群聊：{} 的群主，没有权限将其移出群聊！", groupUser.getGroup().getGName());
            return Result.failed("您不是群聊：" + groupUser.getGroup().getGName() + " 的群主，没有权限将其移出群聊！");
        }

        // 4、移出用户
        int result = groupService.doUpdateUserInGroup(groupUser, false);
        if (1 != result) {
            log.warn("未能成功将此用户移出您的群聊，请稍后再试！");
            return Result.failed("未能成功将此用户移出您的群聊，请稍后再试！");
        }

        log.info("已成功将用户：{} 移出您的群聊：{}！", groupUser.getMember().getNickname(), groupUser.getGroup().getGName());
        return Result.ok("已成功将用户：" + groupUser.getMember().getNickname() +
                " 移出您的群聊：" + groupUser.getGroup().getGName() + "！", groupUser);
    }

    /**
     * 退出群聊
     * @param gCode
     * @return
     */
    @GetMapping("/exit-group/{gCode}")
    public ResultVO doExitGroup(@PathVariable("gCode") String gCode,
                              HttpServletRequest request) {

        // 1、检查内容是否为空
        if (StringUtil.isEmpty(gCode)) {
            log.warn("退出群聊失败，群聊唯一码不能为空！");
            return Result.failed("退出群聊失败，群聊唯一码不能为空！");
        }

        // 2、查询gCode对应的群组信息
        Group exitGroup = groupService.doGetGroupByGCode(gCode);
        if (null == exitGroup) {
            log.warn("退出群聊失败，未找到群聊唯一码为：{} 的群聊信息！", gCode);
            return Result.failed("退出群聊失败，未找到群聊唯一码为：" + gCode + " 的群聊信息！");
        }

        log.info("Exit Group: {}", exitGroup);

        // 3、检查本人是否为该群群主
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        if (thisUser.equals(exitGroup.getHostUser())) {
            log.warn("您是群聊：{} 的群主，无法退出群聊，请使用解散群聊功能！", exitGroup.getGName());
            return Result.failed("您是群聊：" + exitGroup.getGName() + " 的群主，无法退出群聊，请使用解散群聊功能！");
        }

        // 5、查询出本人的入群记录
        GroupUser exitGroupUser = groupService.doCheckUserIsInGroup(exitGroup.getGId(), thisUser.getUId());
        if (null == exitGroupUser) {
            log.warn("您不是群聊：{} 的成员，无法退出群聊！", exitGroup.getGName());
            return Result.failed("您不是群聊：" + exitGroup.getGName() + " 的成员，无法退出群聊！");
        }

        // 6、退出此群聊
        int result = groupService.doUpdateUserInGroup(exitGroupUser, false);
        if (1 != result) {
            log.warn("未能退出群组：{}，请稍后再试！", exitGroup.getGName());
            return Result.failed("未能退出群组：" + exitGroup.getGName() + "，请稍后再试！");
        }

        log.info("已成功退出群组：{}！", exitGroup.getGName());
        return Result.ok("已成功退出群组：" + exitGroup.getGName() + "！", exitGroupUser);
    }

    /**
     * 解散群聊
     * @param gCode
     * @return
     */
    @GetMapping("/dissolve-group/{gCode}")
    public ResultVO doDissolveGroup(@PathVariable("gCode") String gCode,
                                    HttpServletRequest request) {

        // 1、检查内容是否为空
        if (StringUtil.isEmpty(gCode)) {
            log.warn("解散群聊失败，群聊唯一码不能为空！");
            return Result.failed("解散群聊失败，群聊唯一码不能为空！");
        }

        // 2、查询gCode对应的群组信息
        Group dissolveGroup = groupService.doGetGroupByGCode(gCode);
        if (null == dissolveGroup) {
            log.warn("解散群聊失败，未能找到群聊唯一码为：{} 的群组信息！", gCode);
            return Result.failed("解散群聊失败，未能找到群聊唯一码为：" + gCode + " 的群聊信息！");
        }

        log.info("Dissolve Group: {}", dissolveGroup);

        // 3、检查本人是否为该群群主
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        if (!thisUser.equals(dissolveGroup.getHostUser())) {
            log.warn("您不是群聊：{} 的群主，没有权限解散群聊！", dissolveGroup.getGName());
            return Result.failed("您不是群聊：" + dissolveGroup.getGName() + " 的群主，没有权限解散群聊！");
        }

        // 4、解散群聊
        int result = groupService.doDissolveGroup(dissolveGroup);
        if (2 > result) {
            log.warn("未能解散群组：{}，请稍后再试！", dissolveGroup.getGName());
            return Result.failed("未能解散群组：" + dissolveGroup.getGName() + "，请稍后再试！");
        }

        log.info("已成功解散群组：{}！", dissolveGroup.getGName());
        return Result.ok("已成功解散群组：" + dissolveGroup.getGName() + "！", dissolveGroup);
    }
}
