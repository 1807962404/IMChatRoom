package edu.hniu.imchatroom.model.bean.response;

import edu.hniu.imchatroom.model.enums.ResponseCodeEnum;

import java.io.Serializable;

/**
 * 响应结果中间件类
 */
public final class Result implements Serializable {
    private static final long serialVersionUID = 459845634834583L;

    // 响应状态码（成功、警告、失败）
    private static final Integer RESPONSE_SUCCESS_CODE = ResponseCodeEnum.getCode(ResponseCodeEnum.SUCCESS);
    private static final Integer RESPONSE_WARNING_CODE = ResponseCodeEnum.getCode(ResponseCodeEnum.WARNING);
    private static final Integer RESPONSE_FAILED_CODE = ResponseCodeEnum.getCode(ResponseCodeEnum.FAILED);

    public static final ResultVO resultVO = new ResultVO();

    /**
     * Result中每个方法被调用时都会主动调用：清除resultVO数据 方法
     */
    private static void clearResultVO() {
        resultVO.setCode(null);
        resultVO.setMsg(null);
        resultVO.setData(null);
    }

    /**
     * 清空ResultVO，然后设置ResultVO中返回值，并将结果返回
     * @return
     */

    private static ResultVO setResultData(Integer code, String msg) {
        clearResultVO();
        resultVO.setCode(code);
        resultVO.setMsg(msg);
        return resultVO;
    }
    private static ResultVO setResultData(Integer code, Object data) {
        clearResultVO();
        resultVO.setCode(code);
        resultVO.setData(data);
        return resultVO;
    }
    private static ResultVO setResultData(Integer code, String msg, Object data) {
        clearResultVO();
        resultVO.setCode(code);
        resultVO.setMsg(msg);
        resultVO.setData(data);
        return resultVO;
    }

    /**
     * 正常
     * @param msg
     * @return
     */
    public static ResultVO ok(String msg) {

        return setResultData(RESPONSE_SUCCESS_CODE, msg);
    }
    public static ResultVO ok(Object data) {
        return setResultData(RESPONSE_SUCCESS_CODE, data);
    }
    public static ResultVO ok(String msg, Object data) {
        return setResultData(RESPONSE_SUCCESS_CODE, msg, data);
    }

    /**
     * 警告
     * @param msg
     * @return
     */
    public static ResultVO warn(String msg) {
        return setResultData(RESPONSE_WARNING_CODE, msg);
    }

    /**
     * 失败
     * @param msg
     * @return
     */
    public static ResultVO failed(String msg) {
        return setResultData(RESPONSE_FAILED_CODE, msg);
    }
}
