package cc.wutao.exception;

import cc.wutao.constant.MessageConstant;

public class UnauthorizedException extends TokenException{
    public UnauthorizedException() {
    }
    public UnauthorizedException(String msg) {
        super(msg);
    }
}
