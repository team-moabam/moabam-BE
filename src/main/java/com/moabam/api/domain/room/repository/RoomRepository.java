package com.moabam.api.domain.room.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moabam.api.domain.room.Room;

import jakarta.persistence.LockModeType;

public interface RoomRepository extends JpaRepository<Room, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Room> findWithPessimisticLockById(Long id);

	@Query(value = "select distinct rm.* from room rm left join routine rt on rm.id = rt.room_id "
		+ "where rm.title like %:keyword% "
		+ "or rm.manager_nickname like %:keyword% "
		+ "or rt.content like %:keyword% "
		+ "order by rm.id desc limit 11", nativeQuery = true)
	List<Room> searchByKeyword(@Param(value = "keyword") String keyword);

	@Query(value = "select distinct rm.* from room rm left join routine rt on rm.id = rt.room_id "
		+ "where (rm.title like %:keyword% "
		+ "or rm.manager_nickname like %:keyword% "
		+ "or rt.content like %:keyword%) "
		+ "and rm.room_type = :roomType "
		+ "order by rm.id desc limit 11", nativeQuery = true)
	List<Room> searchByKeywordAndRoomType(@Param(value = "keyword") String keyword,
		@Param(value = "roomType") String roomType);

	@Query(value = "select distinct rm.* from room rm left join routine rt on rm.id = rt.room_id "
		+ "where (rm.title like %:keyword% "
		+ "or rm.manager_nickname like %:keyword% "
		+ "or rt.content like %:keyword%) "
		+ "and rm.id < :roomId "
		+ "order by rm.id desc limit 11", nativeQuery = true)
	List<Room> searchByKeywordAndRoomId(@Param(value = "keyword") String keyword, @Param(value = "roomId") Long roomId);

	@Query(value = "select distinct rm.* from room rm left join routine rt on rm.id = rt.room_id "
		+ "where rm.title like %:keyword% "
		+ "or rm.manager_nickname like %:keyword% "
		+ "or rt.content like %:keyword% "
		+ "and rm.room_type = :roomType "
		+ "and rm.id < :roomId "
		+ "order by rm.id desc limit 11", nativeQuery = true)
	List<Room> searchByKeywordAndRoomIdAndRoomType(@Param(value = "keyword") String keyword,
		@Param(value = "roomType") String roomType, @Param(value = "roomId") Long roomId);
}
