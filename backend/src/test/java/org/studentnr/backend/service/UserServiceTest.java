package org.studentnr.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.studentnr.backend.StubApplication;
import org.studentnr.backend.entities.Purchase;
import org.studentnr.backend.entities.Trip;
import org.studentnr.backend.entities.User;

import javax.persistence.Persistence;
import javax.validation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = StubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserServiceTest extends ServiceTestBase{


    @Autowired
    private UserService userService;

    @Autowired
    private TripService tripService;

    @Autowired
    private PurchaseService purchaseService;

    private ValidatorFactory valFactory;
    private Validator validator;

    private String email = "Rosso@Hotmail.com";
    private String name = "Rosso";
    private String midleName = "Melodi";
    private String surename = "Merandi";
    private String address = "someAdress 99";
    private String postalCode = "33rd street";
    private String password = "MyPassword123";

    @BeforeEach
    public void init() {
        valFactory = Validation.buildDefaultValidatorFactory();
        validator = valFactory.getValidator();
    }

    private <T> boolean hasViolations(T obj){
        Set<ConstraintViolation<T>> violations = validator.validate(obj);

        for(ConstraintViolation<T> cv : violations){
            System.out.println("VIOLATION: "+cv.toString());
        }

        return violations.size() > 0;
    }


    public User createValidUser(String email){
        User user = new User();
        user.setEmail(email);
        user.setFirstname(name);
        user.setMiddleName(midleName);
        user.setSurname(surename);
        user.setAddress(address);
        user.setPostalCode(postalCode);
        user.setPassword(password);
        user.setEnabled(true);

        return user;
    }

    @Test
    public void testNoUsers() {
        List<User> list = userService.getAllUsers(false);
        assertEquals(0, list.size());
    }


    @Test
    public void testCreateUser() {

        boolean created = userService.createUser(email, name, midleName, surename, address, postalCode, password);

        assertTrue(created);

        User user = userService.getUser(email, false);

        assertEquals(email, user.getEmail());
        assertThrows(Exception.class, () -> user.getPurchases().size());


    }


    @Test
    public void testCreateTwice(){
        boolean created = userService.createUser(email, name, midleName, surename, address, postalCode, password);
        assertTrue(created);

        boolean reCreated = userService.createUser(email, name, midleName, surename, address, postalCode, password);
        assertFalse(reCreated);
    }


    @Test
    public void testTooShortEmail(){
        String myEMail = "x@f.n";

        User user = createValidUser(myEMail);

        assertTrue( hasViolations( user ) );
    }


    @Test
    public void testTooLongEmail(){
        String x = "";
        for(int i = 0; i < 255; i++){ //max is set to 250 char
            x = x.concat("a");
        }

        String email = "Rossi";
        email = email.concat(x);
        email = email.concat("@Gmail.com");
        User user = createValidUser(email);

        assertTrue( hasViolations( user ) );
    }


    @Test
    public void testPasswordTooLongOrShort(){
        //Same principles here..
    }


    @Test
    public void testGetWithPurchases(){
        boolean created = userService.createUser(email, name, midleName, surename, address, postalCode, password);
         assertTrue( created );
         User user = userService.getUser(email, false);

         LocalDate departureDate= LocalDate.now().plusYears(1);
         LocalDate returnDate= departureDate.plusMonths(1);

         Long trip1 = tripService.createTrip("aaa", "BBB", 4000, "Bahamas", departureDate, returnDate);
         Long trip2 = tripService.createTrip("nnn", "CCCCC", 8000, "Spain", departureDate, returnDate);
         Long trip3 = tripService.createTrip("vvv", "GGG", 9000, "Thailand", departureDate, returnDate);

         Long booked1 = purchaseService.bookTrip(user.getEmail(), trip1);
         Long booked2 = purchaseService.bookTrip(user.getEmail(), trip2);
         Long booked3 = purchaseService.bookTrip(user.getEmail(), trip3);

         User userAfterPurchase = userService.getUser(email, true);

         assertEquals(3, userAfterPurchase.getPurchases().size());
         assertTrue(userAfterPurchase.getPurchases().get(0).getTrip().getLocation() == "Bahamas");
         assertTrue(userAfterPurchase.getPurchases().get(1).getTrip().getLocation() == "Spain");
         assertTrue(userAfterPurchase.getPurchases().get(2).getTrip().getLocation() == "Thailand");

    }












}
