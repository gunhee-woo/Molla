package com.dlog.molla;

/**
 * <pre>
 *     이 프로그램에서 사용될 코드를 관리하는 클래스
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 */
public class Code {
    /**
     * <pre>
     *     상대방에서 보낸 메시지와 내가 보낸 메시지를 구분할 필요가 있다.
     *     채팅방에서 상대방에게 온 메시지느 왼쪽으로 정렬하고 내가 보낸 것들은 오른쪽으로 정렬한다
     *
     * </pre>
     * @author 최정헌
     */
    public class ViewType{
        /**
         * 상대방에게서 온 메시지이므로 왼쪽으로 정렬, left content
         */
        public static final int LEFT_CONTENT = 1;
        /**
         * 내가 보낸 메시지이므로 오른쪽 정렬, right content
         */
        public static final int RIGHT_CONTENT = 2;
    }
}
