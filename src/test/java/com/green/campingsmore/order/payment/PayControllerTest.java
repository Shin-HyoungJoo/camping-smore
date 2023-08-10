package com.green.campingsmore.order.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.campingsmore.MockMvcConfig;
import com.green.campingsmore.config.security.model.MyUserDetails;
import com.green.campingsmore.order.cart.CartController;
import com.green.campingsmore.order.cart.CartMapper;
import com.green.campingsmore.order.cart.CartService;
import com.green.campingsmore.order.cart.CartServiceImpl;
import com.green.campingsmore.order.cart.model.InsCartDto;
import com.green.campingsmore.order.cart.model.InsCartDto1;
import com.green.campingsmore.order.payment.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcConfig
@WebMvcTest(PayController.class)
@AutoConfigureMockMvc(addFilters = false)
class PayControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PayService service;

    @BeforeEach
    void beforeEach() {

        UserDetails user = createUserDetails();

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));

    }

    private UserDetails createUserDetails() {
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");

        UserDetails userDetails = MyUserDetails.builder()
                .iuser(10L)
                .name("김철수")
                .roles(roles)
                .build();
        return userDetails;
    }


    @Test
    @DisplayName("PayController - 결제 정보 저장")
    void postPayInfo() throws Exception {
        //given
        Long testResult = 1L;
        given(service.insPayInfo(any())).willReturn(testResult);

        //when
        InsPayInfoDto item = new InsPayInfoDto();
        item.setAddress("주소주소주소");
        item.setAddressDetail("상세주소주소");
        item.setTotalPrice(50000L);
        item.setShippingPrice(3000L);
        item.setShippingMemo("빨리주세요");

        List<PayDetailInfoVo> itemList = new ArrayList<>();
        PayDetailInfoVo item1 = new PayDetailInfoVo(1L, 2L, 25000L);
        PayDetailInfoVo item2 = new PayDetailInfoVo(2L, 5L, 25000L);

        itemList.add(item1);
        itemList.add(item2);

        item.setPurchaseList(itemList);

        ObjectMapper om = new ObjectMapper();
        String jsonItem = om.writeValueAsString(item);

        //when
        ResultActions ra = mvc.perform(post("/api/payment")
                .content(jsonItem)
                .contentType(MediaType.APPLICATION_JSON));

        //then
        ra.andExpect(status().isOk())
                .andExpect(content().string("1"))
                .andDo(print());

        then(service).should(times(1)).insPayInfo(any());
    }

    @Test
    @DisplayName("PayController - 결제 내역 보기")
    void getPaymentComplete() throws Exception {
        PaymentCompleteDto item = new PaymentCompleteDto();
        item.setAddress("주소주소주소");
        item.setAddressDetail("상세주소주소");
        item.setTotalPrice(65000L);
        item.setShippingMemo("빨리주세요");
        given(service.selPaymentComplete(any())).willReturn(item);

        ResultActions ra = mvc.perform(get("/api/payment/{iorder}", 1L));

        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("주소주소주소"))
                .andExpect(jsonPath("$.addressDetail").value("상세주소주소"))
                .andExpect(jsonPath("$.totalPrice").value(65000L))
                .andExpect(jsonPath("$.shippingMemo").value("빨리주세요"));

        then(service).should(times(1)).selPaymentComplete(any());
    }


    @Test
    @DisplayName("PayController - 전체 결제 내역 보기(마이 페이지)")
    void getPaymentList() throws Exception {
        List<SelPaymentDetailDto> itemList1 = new ArrayList<>();
        SelPaymentDetailDto item = new SelPaymentDetailDto();
        item.setIorder(1L);

        List<PaymentDetailDto2> itemList2 = new ArrayList<>();
        PaymentDetailDto2 item2 = new PaymentDetailDto2();
        item2.setIitem(1L);
        item2.setName("고기");
        item2.setPic("사진.jpg");
        item2.setPaymentDate(LocalDate.parse("2023-01-01"));
        item2.setTotalPrice(65000L);
        itemList2.add(item2);

        PaymentDetailDto2 item3 = new PaymentDetailDto2();
        item3.setIitem(2L);
        item3.setName("라면");
        item3.setPic("사진.jpg");
        item3.setPaymentDate(LocalDate.parse("2023-01-02"));
        item3.setTotalPrice(9000L);
        itemList2.add(item3);

        item.setItemList(itemList2);
        itemList1.add(item);

        given(service.selPaymentDetailAll(any())).willReturn(itemList1);

        ResultActions ra = mvc.perform(get("/api/payment/paymentList", 1L));

        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(itemList1.size())))
                .andExpect(jsonPath("$[*].iorder").exists())
                .andExpect(jsonPath("$[0].itemList", hasSize(itemList2.size())))
                .andExpect(jsonPath("$[0].itemList[0].iitem").value(1L))
                .andExpect(jsonPath("$[0].itemList[0].name").value("고기"))
                .andExpect(jsonPath("$[0].itemList[0].pic").value("사진.jpg"))
                .andExpect(jsonPath("$[0].itemList[0].paymentDate").value("2023-01-01"))
                .andExpect(jsonPath("$[0].itemList[0].totalPrice").value(65000L))

                .andExpect(jsonPath("$[0].itemList[1].iitem").value(2L))
                .andExpect(jsonPath("$[0].itemList[1].name").value("라면"))
                .andExpect(jsonPath("$[0].itemList[1].pic").value("사진.jpg"))
                .andExpect(jsonPath("$[0].itemList[1].paymentDate").value("2023-01-02"))
                .andExpect(jsonPath("$[0].itemList[1].totalPrice").value(9000L))
                .andDo(print());

        then(service).should(times(1)).selPaymentDetailAll(any());
    }


    @Test
    @DisplayName("PayController - 상세 결제 내역 보기(마이 페이지)")
    void getDetailedItemPaymentInfo() {


    }

    @Test
    @DisplayName("PayController - ")
    void delPaymentDetail() {

    }


    @Test
    @DisplayName("PayController - ")
    void getPaymentItemList() {

    }


    @Test
    @DisplayName("PayController - ")
    void getPaymentItem() {
    }



    @Test
    @DisplayName("PayController - 배송지 추가 등록")
    void insAddress() throws Exception {
        //given
        Long testResult = 1L;
        given(service.insAddress(any())).willReturn(testResult);

        ShippingInsDto1 dto = new ShippingInsDto1();
        dto.setAddress("주소주소주소");
        dto.setAddressDetail("상세주소주소");
        dto.setName("신형주");
        dto.setPhone("01022228888");
        dto.setIuser(10L);

        ObjectMapper om = new ObjectMapper();
        String jsonValue = om.writeValueAsString(dto);

        ResultActions ra = mvc.perform(post("/api/payment/address")
                .content(jsonValue)
                .contentType(MediaType.APPLICATION_JSON));

        //then
        ra.andExpect(status().isOk())
                .andExpect(content().string("1"))
                .andDo(print());

        then(service).should(times(1)).insAddress(any());
    }

    @Test
    @DisplayName("PayController - 기본 배송지(유저 주소) 출력")
    void selUserAddress() throws Exception {
        //given
        SelUserAddressVo item = new SelUserAddressVo
                ("주소주소주소", "상세주소주소", "수령인", "01022225555");
        given(service.selUserAddress(any())).willReturn(item);

        //when
        ResultActions ra = mvc.perform(get("/api/payment/address"));

        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$.userAddress").value("주소주소주소"))
                .andExpect(jsonPath("$.userAddressDetail").value("상세주소주소"))
                .andExpect(jsonPath("$.name").value("수령인"))
                .andExpect(jsonPath("$.phone").value("01022225555"))
                .andDo(print());

        //then
        then(service).should(times(1)).selUserAddress(any());
    }

    @Test
    @DisplayName("PayController - 등록된 배송지 리스트 출력")
    void selAddressList() throws Exception {
        //given
        List<ShippingListSelVo> itemList = new ArrayList<>();
        ShippingListSelVo item1 = new ShippingListSelVo
                ("주소주소주소1", "상세주소주소1", "수령인1", "01022225555");
        ShippingListSelVo item2 = new ShippingListSelVo
                ("주소주소주소2", "상세주소주소2", "수령인2", "01055552222");
        itemList.add(item1);
        itemList.add(item2);

        given(service.selAddressList(any())).willReturn(itemList);

        //when
        ResultActions ra = mvc.perform(get("/api/payment/addressList"));

        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(itemList.size())))
                .andExpect(jsonPath("$[*].address").exists())
                .andExpect(jsonPath("$[*].addressDetail").exists())
                .andExpect(jsonPath("$[*].name").exists())
                .andExpect(jsonPath("$[*].phone").exists())
                .andExpect(jsonPath("$[0].address").value("주소주소주소1"))
                .andExpect(jsonPath("$[0].addressDetail").value("상세주소주소1"))
                .andExpect(jsonPath("$[0].name").value("수령인1"))
                .andExpect(jsonPath("$[0].phone").value("01022225555"))

                .andExpect(jsonPath("$[1].address").value("주소주소주소2"))
                .andExpect(jsonPath("$[1].addressDetail").value("상세주소주소2"))
                .andExpect(jsonPath("$[1].name").value("수령인2"))
                .andExpect(jsonPath("$[1].phone").value("01055552222"))
                .andDo(print());

        //then
        then(service).should(times(1)).selAddressList(any());
    }

    @Test
    @DisplayName("PayController - 등록된 배송지 중 선택한 배송지 정보 출력")
    void selOneAddress() throws Exception {
        //given
        ShippingListSelVo item = new ShippingListSelVo
                ("주소주소주소1", "상세주소주소1", "수령인1", "01022225555");

        given(service.selOneAddress(any())).willReturn(item);

        //when
        ResultActions ra = mvc.perform(get("/api/payment/addressList/{iaddress}", 1L));

        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("주소주소주소1"))
                .andExpect(jsonPath("$.addressDetail").value("상세주소주소1"))
                .andExpect(jsonPath("$.name").value("수령인1"))
                .andExpect(jsonPath("$.phone").value("01022225555"))
                .andDo(print());
        //then
        then(service).should(times(1)).selOneAddress(any());

    }
}