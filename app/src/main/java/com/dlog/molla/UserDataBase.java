package com.dlog.molla;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * <pre>
 *     Entity {@link User} 클래스를 저장하는 Room 데이터베이스 클래스
 *     RoomDatabase를 상속한다.
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 * @see <a href="https://developer.android.com/reference/android/arch/persistence/room/RoomDatabase">RoomDatabase</a>
 */
@Database(entities = {User.class},version = 2)
public abstract class UserDataBase extends RoomDatabase {
    /**
     *  RoomDatabase에 접근하는 Dao 클래스
     * @return UserDao
     */
    public abstract UserDao userDao();
}
