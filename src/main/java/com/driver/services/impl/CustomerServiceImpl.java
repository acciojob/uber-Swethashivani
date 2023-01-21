package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.CabRepository;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    CustomerRepository customerRepository2;

    @Autowired
    DriverRepository driverRepository2;

    @Autowired
    TripBookingRepository tripBookingRepository2;

    @Autowired
    CabRepository cabRepository;

    @Override
    public void register(Customer customer) {
        //Save the customer in database
        customerRepository2.save(customer);
    }

    @Override
    public void deleteCustomer(Integer customerId) {
        // Delete customer without using deleteById function
        Customer customer = customerRepository2.findById(customerId).get();
        customerRepository2.delete(customer);

    }

    @Override
    public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception {
        //Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
        //Avoid using SQL query

        List<Driver> drivers = driverRepository2.findAll();
        drivers.sort(Comparator.comparingInt(Driver::getDriverId));
        Driver requiredDriver = null;
        for (Driver driver : drivers) {
            if (driver.getCab().getAvailable()) {
                requiredDriver = driver;
                break;
            }
        }
        if (requiredDriver == null)
            throw new Exception("No cab available!");

        TripBooking tripBooking=new TripBooking(fromLocation,toLocation, distanceInKm,TripStatus.CONFIRMED);


        requiredDriver.getCab().setAvailable(false);
        tripBooking.setBill(requiredDriver.getCab().getPerKmRate()*distanceInKm);
        Customer customer=customerRepository2.findById(customerId).get();

        tripBooking.setDriver(requiredDriver);
        tripBooking.setCustomer(customer);

        requiredDriver.getTripBookingList().add(tripBooking);
        customer.getTripBookingList().add(tripBooking);

//		driverRepository2.save(driver);
//		customerRepository2.save(customer);

        tripBookingRepository2.save(tripBooking);

        return tripBooking;
    }

    @Override
    public void cancelTrip(Integer tripId) {
        //Cancel the trip having given trip Id and update TripBooking attributes accordingly
        TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
        tripBooking.setStatus(TripStatus.CANCELED);
        tripBooking.setBill(0);
        Driver driver = tripBooking.getDriver();
        driver.getCab().setAvailable(true);
        tripBookingRepository2.save(tripBooking);
    }

    @Override
    public void completeTrip(Integer tripId) {
        //Complete the trip having given trip Id and update TripBooking attributes accordingly
        TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
        tripBooking.setStatus(TripStatus.COMPLETED);
        Driver driver = tripBooking.getDriver();
         driver.getCab().setAvailable(true);
        tripBookingRepository2.save(tripBooking);


    }
}