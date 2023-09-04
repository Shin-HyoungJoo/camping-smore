package com.green.campingsmore.user.camping;

import com.green.campingsmore.community.board.utils.FileUtils;
import com.green.campingsmore.config.security.AuthenticationFacade;
import com.green.campingsmore.entity.*;
import com.green.campingsmore.sign.SignRepository;
import com.green.campingsmore.user.camping.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampingService {
    private final CampingRepository REP;
    private final CityRepository CITYREP;
    private final CampingRepositoryImpl IMPL;
    private final CampingPicRepository PICREP;
    private final ReserveRepository RESREP;
    private final AuthenticationFacade FACADE;
    private final SignRepository USERREP;


    @Value("${file.dir}")
    private String fileDir;

    public CampingRes InsCamp(MultipartFile pic, CampingDto dto) throws Exception {

        if (pic != null) {
            String originFile = pic.getOriginalFilename();
            String saveName = FileUtils.makeRandomFileNm(originFile);
            NationwideEntity nationwideEntity = NationwideEntity.builder()
                    .inationwide(dto.getInationwide())
                    .build();
            CITYREP.save(nationwideEntity);
            CampEntity campEntity = CampEntity.builder()
                    .campPhone(dto.getCampPhone())
                    .name(dto.getName())
                    .address(dto.getAddress())
                    .reservation(dto.getReservation())
                    .capacity(dto.getCapacity())
                    .note(dto.getNote())
                    .mainPic(saveName)
                    .price(dto.getPrice())
                    .quantity(dto.getQuantity())
                    .delyn(1)
                    .nationwideEntity(nationwideEntity)
                    .build();
            REP.save(campEntity);

            String centerPath = String.format("campPic/%d", campEntity.getIcamp());
            String targetPath = String.format("%s/%s", FileUtils.getAbsolutePath(fileDir), centerPath);
            File file = new File(targetPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            File fileTarget = new File(targetPath + "/" + saveName);
            campEntity.setIcamp(campEntity.getIcamp());
            try {
                pic.transferTo(fileTarget);
            } catch (IOException e) {
                throw new Exception("파일저장을 실패했습니다");
            }
            campEntity.setMainPic("campPic/" + campEntity.getIcamp() + "/" + saveName);
            REP.save(campEntity);

            log.info("{}", campEntity);

            return CampingRes.builder().icamp(campEntity.getIcamp())
                    .campPhone(campEntity.getCampPhone())
                    .name(campEntity.getName())
                    .inationwide(nationwideEntity.getInationwide())
                    .address(campEntity.getAddress())
                    .capacity(campEntity.getCapacity())
                    .mainPic(campEntity.getMainPic())
                    .price(campEntity.getPrice())
                    .note(campEntity.getNote())
                    .quantity(campEntity.getQuantity())
                    .delyn(campEntity.getDelyn())
                    .reservation(campEntity.getReservation())
                    .build();
        }
        return null;
    }

    public CampingRes updCamping(MultipartFile pic, CampingUpdDto dto) throws Exception {
        Optional<CampEntity> opt = REP.findById(dto.getIcamp());

        if (!opt.isPresent()) {
            return null;
        }
        CampEntity entity = opt.get();
        if (pic != null) {
            String oldFilePath = entity.getMainPic();
            if (oldFilePath != null) {
                File oldFile = new File(FileUtils.getAbsolutePath(fileDir) + "/" + oldFilePath);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
            String originFile = pic.getOriginalFilename();
            String saveName = FileUtils.makeRandomFileNm(originFile);
            NationwideEntity nationwideEntity = NationwideEntity.builder()
                    .inationwide(dto.getInationwide())
                    .build();
            CITYREP.save(nationwideEntity);
            CampEntity campEntity = CampEntity.builder()
                    .icamp(dto.getIcamp())
                    .campPhone(dto.getCampPhone())
                    .name(dto.getName())
                    .address(dto.getAddress())
                    .price(dto.getPrice())
                    .reservation(dto.getReservation())
                    .capacity(dto.getCapacity())
                    .nationwideEntity(nationwideEntity)
                    .note(dto.getNote())
                    .mainPic(saveName)
                    .quantity(dto.getQuantity())
                    .delyn(1)
                    .build();
            REP.save(campEntity);

            String centerPath = String.format("campPic/%d", campEntity.getIcamp());
            String targetPath = String.format("%s/%s", FileUtils.getAbsolutePath(fileDir), centerPath);
            File file = new File(targetPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            File fileTarget = new File(targetPath + "/" + saveName);
            entity.setIcamp(campEntity.getIcamp());
            entity.setMainPic(centerPath + "/" + saveName);
            try {
                pic.transferTo(fileTarget);
            } catch (IOException e) {
                throw new Exception("파일저장을 실패했습니다");
            }
            campEntity.setMainPic("campPic/" + campEntity.getIcamp() + "/" + saveName);
            REP.save(campEntity);

            return CampingRes.builder().icamp(campEntity.getIcamp())
                    .campPhone(campEntity.getCampPhone())
                    .name(campEntity.getName())
                    .inationwide(dto.getInationwide())
                    .address(campEntity.getAddress())
                    .capacity(campEntity.getCapacity())
                    .reservation(campEntity.getReservation())
                    .price(campEntity.getPrice())
                    .mainPic(campEntity.getMainPic())
                    .note(campEntity.getNote())
                    .quantity(campEntity.getQuantity())
                    .delyn(campEntity.getDelyn())
                    .build();
        }
        return null;
    }

    public CampingRes delCamping(CampingDelDto dto) {
        Optional<CampEntity> opt = REP.findById(dto.getIcamp());
        if (!opt.isPresent()) {
            return null;
        }

        CampEntity entity = opt.get();
        int newDelyn = (entity.getDelyn() == 0) ? 1 : 0;
        entity.setDelyn(newDelyn);
        REP.save(entity);

        // 기존 CampingDto를 사용하여 값을 가져오도록 수정
        CampingDto campingDto = new CampingDto(); // 여기에 값을 초기화해야 함
        campingDto.setInationwide(entity.getNationwideEntity().getInationwide());

        NationwideEntity nationwideEntity = NationwideEntity.builder()
                .inationwide(campingDto.getInationwide())
                .build();
        CITYREP.save(nationwideEntity);
        CampEntity campEntity = opt.get();

        return CampingRes.builder()
                .icamp(campEntity.getIcamp())
                .campPhone(campEntity.getCampPhone())
                .reservation(campEntity.getReservation())
                .name(campEntity.getName())
                .inationwide(nationwideEntity.getInationwide()) // 기존 entity에서 가져오도록 수정
                .address(campEntity.getAddress())
                .capacity(campEntity.getCapacity())
                .mainPic(campEntity.getMainPic())
                .note(campEntity.getNote())
                .price(campEntity.getPrice())
                .quantity(campEntity.getQuantity())
                .delyn(entity.getDelyn())
                .build();
    }


    public List<String> InsPic(List<MultipartFile> pics, CampingPicDto dto) throws Exception {
        List<String> fileUrls = new ArrayList<>();
        CampEntity campEntity = new CampEntity();
        if (pics != null) {
            campEntity.setIcamp(dto.getIcamp());
            String centerPath = String.format("campPics/%d", campEntity.getIcamp());
            String targetPath = String.format("%s/%s", FileUtils.getAbsolutePath(fileDir), centerPath);
            File file = new File(targetPath);
            if (!file.exists()) {
                file.mkdirs();
            }

            for (MultipartFile pic : pics) {
                String originFile = pic.getOriginalFilename();
                String saveName = FileUtils.makeRandomFileNm(originFile);

                File fileTarget = new File(targetPath + "/" + saveName);
                try {
                    pic.transferTo(fileTarget);
                } catch (IOException e) {
                    throw new Exception("파일저장을 실패했습니다");
                }
                CampPicEntity campPicEntity = CampPicEntity.builder()
                        .campEntity(campEntity)
                        .pic("campPics/" + campEntity.getIcamp() + "/" + saveName)
                        .build();
//                campPicEntity.setCampEntity(campEntity);
//                campPicEntity.setPic();
                PICREP.save(campPicEntity);

                fileUrls.add("campPics/" + campEntity.getIcamp() + "/" + saveName);
            }

        }
        return fileUrls;
    }

    public Long delPic(CampingPicDelDto dto) {
        Optional<CampPicEntity> opt = PICREP.findById(dto.getIcampPic());
        if (!opt.isPresent()) {
            return null;
        }
        CampPicEntity entity = opt.get();

        // 파일을 삭제할 디렉토리 경로
        String centerPath = String.format("campPics/%d", dto.getIcamp());
        String targetPath = String.format("%s/%s", FileUtils.getAbsolutePath(fileDir), centerPath);
        String result = entity.getPic();
        String picName = result.substring(result.lastIndexOf('/') + 1);
        File fileToDelete = new File(targetPath, picName);

        // 파일 삭제
        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                // 파일 삭제에 성공하면 엔티티를 영구적으로 삭제
                PICREP.delete(entity);
                return entity.getIcampPic();
            } else {
                // 파일 삭제 실패 처리
                return null;
            }
        } else {
            // 파일이 존재하지 않는 경우 처리
            return null;
        }
    }

    public ReserveRes InsReserve(ReserveDto dto) throws Exception {
        Optional<CampEntity> opt = REP.findById(dto.getIcamp());
        if (!opt.isPresent()) {
            return null;
        }
        CampEntity entity = opt.get();
        int currentQuantity = entity.getQuantity();
        LocalDate reserveDay = dto.getReservation(); // 예약 날짜를 dto에서 가져옵니다.

        LocalDate today = LocalDate.now();

        LocalDate maxReservationDate = today.plusDays(30);

        if (reserveDay.isBefore(today) || reserveDay.isAfter(maxReservationDate)) {
            throw new Exception("예약 불가: 예약 가능한 날짜는 오늘부터 30일 이후까지입니다.");
        }

        if (currentQuantity <= 0) {
            throw new Exception("예약 불가: 캠핑장이 모두 예약되었습니다.");
        }

        entity.setIcamp(dto.getIcamp());
        entity.setQuantity(currentQuantity - 1);
        entity.setReservation(reserveDay);

        UserEntity userEntity = UserEntity.builder()
                .iuser(FACADE.getLoginUserPk())
                .build();
        ReserveEntity reserveEntity = ReserveEntity.builder()
                .name(dto.getName())
                .campEntity(entity)
                .userEntity(userEntity)
                .payType(dto.getPayType())
                .phone(dto.getPhone())
                .payStatus(PayStatus.OK)
                .build();
        RESREP.save(reserveEntity);

        return ReserveRes.builder()
                .ireserve(reserveEntity.getIreserve())
                .icamp(entity.getIcamp())
                .price(entity.getPrice())
                .iuser(userEntity.getIuser())
                .name(reserveEntity.getName())
                .payType(reserveEntity.getPayType())
                .phone(reserveEntity.getPhone())
                .payStatus(reserveEntity.getPayStatus())
                .campEntity(entity).build();
    }
    public ReserveRes cancelReserve(ReserveCancelDto dto) throws Exception {
        Optional<ReserveEntity> opt = RESREP.findById(dto.getIreserve());
        if (!opt.isPresent()) {
            throw new NotFoundException("예약을 찾을 수 없습니다.");
        }

        ReserveEntity reserveEntity = opt.get();
        UserEntity userEntity = UserEntity.builder().iuser(FACADE.getLoginUserPk()).build();
        CampEntity campEntity = reserveEntity.getCampEntity();

        LocalDate reservationDate = campEntity.getReservation();
        LocalDate currentDate = LocalDate.now();
        long daysUntilReservation = ChronoUnit.DAYS.between(currentDate, reservationDate);

        if (daysUntilReservation <= 1) {
            throw new Exception("예약 날짜가 1일 이내이므로 취소가 불가능합니다.");
        }

        if (reserveEntity.getPayStatus() == PayStatus.OK) {
            campEntity.setQuantity(campEntity.getQuantity() + 1);
            ReserveEntity reserve = ReserveEntity.builder()
                    .ireserve(reserveEntity.getIreserve())
                    .campEntity(campEntity)
                    .userEntity(userEntity)
                    .payStatus(PayStatus.CANCEL)
                    .build();
            RESREP.save(reserve);
        }

        return ReserveRes.builder()
                .ireserve(reserveEntity.getIreserve())
                .build();
    }
    public ReserveRes updReserve(ReserveUpdDto dto) throws NotFoundException {
        Optional<ReserveEntity> opt = RESREP.findById(dto.getIreserve());
        if (!opt.isPresent()) {
            throw new NotFoundException("예약을 찾을 수 없습니다.");
        }

        ReserveEntity reserveEntity = opt.get();
        UserEntity userEntity = UserEntity.builder().iuser(FACADE.getLoginUserPk()).build();

        reserveEntity.setUserEntity(userEntity);
        reserveEntity.setName(dto.getName());
        reserveEntity.setPhone(dto.getPhone());


        RESREP.save(reserveEntity);

        return ReserveRes.builder()
                .ireserve(reserveEntity.getIreserve())
                .iuser(userEntity.getIuser())
                .name(reserveEntity.getName())
                .phone(reserveEntity.getPhone())
                .build();
    }
    public List<CampingList> getCamping(Long inationwide) {
        LocalDate today = LocalDate.now(); // 현재 날짜를 가져옴
        List<CampingList> list = REP.selCamping(inationwide, today, 1); // 오늘 날짜 정보를 파라미터로 전달
        return list;
    }
    public List<CampingList> getCampingAll(){
        LocalDate today = LocalDate.now();
        List<CampingList> list = REP.selCampingAll(today,1);
        return list;
    }
    public List<CampingDetailList> getDeCamping(Long icamp) {
        List<CampingDetailList> results = REP.selDeCamping(icamp);

        Set<Long> uniqueIcampSet = new HashSet<>();
        List<CampingDetailList> uniqueResults = new ArrayList<>();
        for (CampingDetailList result : results) {
            if (uniqueIcampSet.add(result.getIcamp())) {
                uniqueResults.add(result);
            }
        }

        for (CampingDetailList result : uniqueResults) {
            List<String> picList = new ArrayList<>();
            for (CampingDetailList resultWithSameIcamp : results) {
                if (result.getIcamp().equals(resultWithSameIcamp.getIcamp())) {
                    picList.addAll(resultWithSameIcamp.getPic());
                }
            }
            result.setPic(picList);
        }

        return uniqueResults;
    }
    public List<CampingMyList> getMyList(){
        List<CampingMyList> list = REP.selMyList(FACADE.getLoginUserPk());
        return list;
    }
}