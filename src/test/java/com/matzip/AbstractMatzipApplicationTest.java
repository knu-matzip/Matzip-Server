package com.matzip;

import com.matzip.fixture.PlaceFixtures;
import com.matzip.fixture.UserFixtures;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.infra.repository.PlaceRepository;
import com.matzip.user.domain.User;
import com.matzip.user.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class AbstractMatzipApplicationTest {

    protected static final Logger log = LoggerFactory.getLogger(AbstractMatzipApplicationTest.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlaceRepository placeRepository;

    protected User testUser;
    protected List<Place> testPlaces;

    @BeforeEach
    void setUp() {
        User user = UserFixtures.createUserWith("test");
        this.testUser = userRepository.save(user);

        List<Place> places = IntStream.range(0, 10)
                .mapToObj(index -> PlaceFixtures.createPlaceWith(String.valueOf(index), this.testUser))
                .toList();
        this.testPlaces = placeRepository.saveAll(places);
    }
}
