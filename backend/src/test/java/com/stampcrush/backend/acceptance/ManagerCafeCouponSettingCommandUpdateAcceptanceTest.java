package com.stampcrush.backend.acceptance;

import com.stampcrush.backend.entity.cafe.Cafe;
import com.stampcrush.backend.entity.cafe.CafeCouponDesign;
import com.stampcrush.backend.entity.cafe.CafePolicy;
import com.stampcrush.backend.entity.cafe.CafeStampCoordinate;
import com.stampcrush.backend.fixture.OwnerFixture;
import com.stampcrush.backend.repository.cafe.CafeCouponDesignRepository;
import com.stampcrush.backend.repository.cafe.CafePolicyRepository;
import com.stampcrush.backend.repository.cafe.CafeRepository;
import com.stampcrush.backend.repository.cafe.CafeStampCoordinateRepository;
import com.stampcrush.backend.repository.user.OwnerRepository;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static com.stampcrush.backend.acceptance.step.ManagerCafeCouponSettingUpdateStep.CAFE_COUPON_SETTING_UPDATE_REQUEST;
import static com.stampcrush.backend.fixture.CafeFixture.cafeOfSavedOwner;
import static com.stampcrush.backend.fixture.CouponDesignFixture.cafeCouponDesignOfSavedCafe;
import static com.stampcrush.backend.fixture.CouponPolicyFixture.cafePolicyOfSavedCafe;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ManagerCafeCouponSettingCommandUpdateAcceptanceTest extends AcceptanceTest {

    @Autowired
    private CafeRepository cafeRepository;

    @Autowired
    private CafeCouponDesignRepository cafeCouponDesignRepository;

    @Autowired
    private CafePolicyRepository cafePolicyRepository;

    @Autowired
    private CafeStampCoordinateRepository cafeStampCoordinateRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Test
    void 카페_사장은_쿠폰_세팅에_대한_내용을_수정할_수_있다() {
        // given, when
        Cafe savedCafe = cafeRepository.save(cafeOfSavedOwner(ownerRepository.save(OwnerFixture.GITCHAN)));

        CafePolicy savedCafePolicy = cafePolicyRepository.save(cafePolicyOfSavedCafe(savedCafe));
        CafeCouponDesign savedCafeCouponDesign = cafeCouponDesignRepository.save(cafeCouponDesignOfSavedCafe(savedCafe));

        CafeStampCoordinate savedCafeStampCoordinate1 = cafeStampCoordinateRepository.save(
                new CafeStampCoordinate(
                        1, 1, 1, savedCafeCouponDesign
                )
        );

        CafeStampCoordinate savedCafeStampCoordinate2 = cafeStampCoordinateRepository.save(
                new CafeStampCoordinate(
                        1, 2, 1, savedCafeCouponDesign
                )
        );

        ExtractableResponse<Response> response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .auth().preemptive().basic(OwnerFixture.GITCHAN.getLoginId(), OwnerFixture.GITCHAN.getEncryptedPassword())
                .body(CAFE_COUPON_SETTING_UPDATE_REQUEST)

                .when()
                .post("/api/admin/coupon-setting?cafe-id=" + savedCafe.getId())

                .then()
                .log().all()
                .extract();

        // then
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value()),
                () -> assertThat(cafeCouponDesignRepository.findById(savedCafeCouponDesign.getId())).isEmpty(),
                () -> assertThat(cafeCouponDesignRepository.findByCafe(savedCafe)).isNotEmpty(),
                () -> assertThat(cafePolicyRepository.findById(savedCafePolicy.getId())).isEmpty(),
                () -> assertThat(cafePolicyRepository.findByCafe(savedCafe)).isNotEmpty()
        );
    }
}