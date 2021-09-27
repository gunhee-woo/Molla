package com.dlog.molla;

import com.squareup.otto.Bus;

/**
 * <pre>
 *     Bus 객체를 제공하는 클래스
 *     싱글톤 형식으로 디자인되어있다.
 *     Publisher가 Event를 post() 하면 Bus에서 Event를 Subscriber에게 브로드캐스트방식으로 전달한다.
 *     Subscriber는 Bus에 regist되어 있어야 Event를 수신할 수 있다.
 *     Bus 클래스는 Square에서 만든 event bus형태의 라이브러리이다.
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 * @see <a href="https://github.com/square/otto">square otto</a>
 */
public class BusProvider {
    /**
     * Bus 인스턴스를 static final로 생성한다.
     */
    private static final Bus BUS = new Bus();

    /**
     * Bus 인스턴스를 반환한다
     * @return {@link BusProvider#BUS}를 반환한다.
     */
    public static Bus getInstance(){
        return BUS;
    }
    private void BusProvider(){
        //no instances.
    }
}
