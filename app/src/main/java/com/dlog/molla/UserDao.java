package com.dlog.molla;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * <pre>
 *     {@link UserDataBase}와 관련된 작업들 (query, insert, update, delete .. )을 정의하는 interface
 * </pre>
 *
 * @author 최정헌
 * @version 1.0.0 20/04/13
 * @see Dao
 * @see Query
 */
@Dao
public interface UserDao {
    /**
     * id 순서로 정렬된 User 리스트를 반환하는 메서드
     * @return 모든 User 리스트
     */
    @Query("SELECT * FROM user ORDER BY mPhoneNum DESC")
    List<User> getAllPerson();

    /**
     * 파라미터로 받은 전화번호에 해당하는 User의 이름을 반환하는 메서드
     * @param num 전화번호
     * @return 이름
     */
    @Query("SELECT mNickName FROM user WHERE mPhoneNum = :num")
    public String getUserName(String num);

    /**
     * <pre>
     *     User Array를 받아서 데이터베이스에 저장하는 메서드
     *     이미 저장된 항목이 있을 경우 데이터를 덮어쓴다.
     * </pre>
     * @param users User Array
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)//중복 primary key 있을 경우 데이터를 덮어씁니다.
    void insertUsers(User... users);

    /**
     * User array를 받아서 데이터베이스에 해당 User들을 삭제하는 메서드
     * @param users User Array
     */
    @Delete
    void deleteUsers(User... users);
}
