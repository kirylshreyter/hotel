package com.kirylshreyter.training.hotel.services;

import java.util.List;
import com.kirylshreyter.training.hotel.datamodel.BookedRoom;

public interface BookedRoomService {
	
	void saveAll(List<BookedRoom> bookedRooms);

	void save(BookedRoom bookedRoom);

	BookedRoom get(Long id);

}