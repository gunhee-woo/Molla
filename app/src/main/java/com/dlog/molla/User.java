package com.dlog.molla;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * <pre>
 *     사용자가 추가하거나, 주소록에서 가져온 친구에 해당하는 클래스
 *     intent에 전달하기 위해 Serializable을 implements한다.
 *     PrimaryKey로 {@link User#mPhoneNum},즉 전화번호 값을 사용한다.
 * </pre>
 * @author 최정헌
 * @version 1.0.1 20/04/20
 * @see Serializable
 * @see Entity
 */
@Entity
public class User implements Serializable {

    /**
     * 이름
     */
    public String mNickName;
    /**
     * 전화번호
     */
    @PrimaryKey
    @NonNull
    public String mPhoneNum;
    /**
     * 선택되었는 가를 나타내는 boolean 값 , 데이터베이스에 저장시키지 않기 위해 Ignore annotation을 사용한다.
     */
    @Ignore Boolean isCheck = false;
    public User(String nickName, String phoneNum){
        this.mNickName = nickName;
        this.mPhoneNum = phoneNum;
    }
}
