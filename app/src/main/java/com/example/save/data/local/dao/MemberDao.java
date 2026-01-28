package com.example.save.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.save.data.local.entities.MemberEntity;

import java.util.List;

/**
 * Data Access Object for Member operations
 */
@Dao
public interface MemberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MemberEntity member);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MemberEntity> members);

    @Update
    void update(MemberEntity member);

    @Delete
    void delete(MemberEntity member);

    @Query("SELECT * FROM members ORDER BY id DESC")
    LiveData<List<MemberEntity>> getAllMembers();

    @Query("SELECT * FROM members ORDER BY id DESC")
    List<MemberEntity> getAllMembersSync();

    @Query("SELECT * FROM members WHERE id = :memberId")
    LiveData<MemberEntity> getMemberById(String memberId);

    @Query("SELECT * FROM members WHERE name = :name LIMIT 1")
    MemberEntity getMemberByName(String name);

    // Authentication query
    @Query("SELECT * FROM members WHERE email = :email LIMIT 1")
    MemberEntity getMemberByEmail(String email);

    @Query("SELECT * FROM members WHERE phone = :phone LIMIT 1")
    MemberEntity getMemberByPhone(String phone);

    @Query("SELECT * FROM members WHERE name LIKE '%' || :query || '%' OR role LIKE '%' || :query || '%' ORDER BY id DESC")
    LiveData<List<MemberEntity>> searchMembers(String query);

    @Query("SELECT * FROM members WHERE role IN ('Administrator', 'Admin') ORDER BY id DESC")
    List<MemberEntity> getAdmins();

    @Query("SELECT COUNT(*) FROM members WHERE role IN ('Administrator', 'Admin')")
    int getAdminCount();

    @Query("SELECT COUNT(*) FROM members WHERE role IN ('Administrator', 'Admin')")
    LiveData<Integer> getAdminCountLive();

    @Query("SELECT * FROM members WHERE isActive = 1")
    List<MemberEntity> getActiveMembers();

    @Query("SELECT COUNT(*) FROM members")
    int getMemberCount();

    @Query("SELECT COUNT(*) FROM members WHERE isActive = 1")
    int getActiveMemberCount();

    @Query("SELECT * FROM members WHERE shortfallAmount > 0")
    List<MemberEntity> getMembersWithShortfalls();

    @Query("DELETE FROM members")
    void deleteAll();

    @Query("UPDATE members SET contributionPaid = 0")
    void resetAllContributions();

    @Query("UPDATE members SET nextPaymentDueDate = :nextDate")
    void updateAllDueDates(String nextDate);
}
