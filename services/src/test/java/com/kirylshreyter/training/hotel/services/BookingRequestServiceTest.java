package com.kirylshreyter.training.hotel.services;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.kirylshreyter.training.hotel.daodb.util.DateConverter;
import com.kirylshreyter.training.hotel.datamodel.BookingRequest;
import com.kirylshreyter.training.hotel.datamodel.Client;
import com.kirylshreyter.training.hotel.datamodel.Room;
import com.kirylshreyter.training.hotel.datamodel.RoomDetails;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:service-context.xml")
public class BookingRequestServiceTest {

	@Inject
	private ClientService clientService;

	@Inject
	private CommonService commonService;

	@Inject
	private RoomService roomService;

	@Inject
	private RoomDetailsService roomDetailsService;

	@Inject
	private DateConverter dateConverter;

	@Inject
	private BookingRequestService bookingRequestService;

	private RoomDetails prepareRoomDetailsWithInsert() {
		RoomDetails roomDetails = new RoomDetails();
		roomDetails.setNumberOfPlaces(1);
		roomDetails.setRoomType("economy");
		roomDetails.setCostPerNight(100.00);
		roomDetails.setAdditionalInformation("1st floor");
		roomDetails.setId(roomDetailsService.save(roomDetails));
		return roomDetails;
	}

	private Room prepareRoomWithInsert(RoomDetails insertedRoomDetails) {
		Room room = new Room();
		room.setNumber("1A");
		room.setStatus("available");
		room.setRoomDetailsId(insertedRoomDetails.getId());
		room.setId(roomService.save(room));
		return room;
	}

	private Client prepareClientWithInsert() {
		Client client = new Client();
		client.setFirstName("Иван");
		client.setLastName("Иванов");
		client.setPhone("+375297800000");
		client.setEmail("ivanov@gmail.com");
		client.setAddress("Республика Беларусь, г. Минск, ул. В. Хоружей, д.1, кв. 1");
		client.setId(clientService.save(client));
		return client;
	}

	@Test
	// @Ignore
	@Transactional
	public void getTest() {
		RoomDetails insertedRoomDetails = prepareRoomDetailsWithInsert();
		Room room = prepareRoomWithInsert(insertedRoomDetails);
		Client client = prepareClientWithInsert();
		BookingRequest bookingRequest = new BookingRequest();
		bookingRequest.setRoomId(room.getId());
		bookingRequest.setClientId(client.getId());
		bookingRequest.setArrivalDate(dateConverter.stringToJavaUtilDateConverter("2017-01-01"));
		bookingRequest.setDepartureDate(dateConverter.stringToJavaUtilDateConverter("2017-01-05"));
		bookingRequest.setId(bookingRequestService.save(bookingRequest));
		BookingRequest entity = (BookingRequest) commonService.get(new BookingRequest(), bookingRequest.getId());
		Assert.assertNotNull("Getted from DB object should not be null.", entity);
		Assert.assertEquals("Id for inserted and selected objects should be the same.", bookingRequest.getId(),
				entity.getId());
		Assert.assertEquals("Inserted object is not the object which getted from database.", bookingRequest, entity);
		commonService.delete(entity, entity.getId());
		commonService.delete(client, client.getId());
		commonService.delete(room, room.getId());
		commonService.delete(insertedRoomDetails, insertedRoomDetails.getId());
	}

}