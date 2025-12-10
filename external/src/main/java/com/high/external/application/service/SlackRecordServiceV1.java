package com.high.external.application.service;

import com.high.external.application.dto.response.SlackRecordDeleteDto;
import com.high.external.application.dto.response.SlackRecordDto;
import com.high.external.domain.exception.SlackRecordNotFoundException;
import com.high.external.domain.model.SlackRecord;
import com.high.external.domain.repository.SlackRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SlackRecordServiceV1 {

    private final SlackRecordRepository repository;


    /**
     * <p>SlackRecord 생성</p>
     *
     * @param recipientId 수령인 식별자
     * @param message     메세지 내용
     */
    public void createSlackRecord(String recipientId, String message) {
        SlackRecord slackRecord = SlackRecord.create(recipientId, message);
        repository.save(slackRecord);
    }


    /**
     * <p>특정 SlackRecord 조회</p>
     *
     * @param slackRecordId 식별자
     * @return {@link SlackRecordDto}
     */
    public SlackRecordDto getSlackRecord(UUID slackRecordId) {
        SlackRecord slackRecord = getRecord(slackRecordId);
        return SlackRecordDto.from(slackRecord);
    }

    /**
     * <p>특정 SlackRecord 리스트 조회</p>
     *
     * @param page      조회할 페이지 번호
     * @param size      한 페이지당 SlackRecord의 개수
     * @param sortBy    정렬 기준이 되는 필드 이름 (예: "createdAt")
     * @param isAsc     오름차순 정렬 여부 (true: 오름차순, false: 내림차순)
     * @return {@link SlackRecordDto}
     */
    public Page<SlackRecordDto> getSlackRecordList(int page, int size, String sortBy, boolean isAsc){
        Pageable pageable = createPageable(page, size, sortBy, isAsc);
        Page<SlackRecord> slackRecordPage = repository.findAll(pageable);
        return slackRecordPage.map(SlackRecordDto::from);
    }


    /**
     * <p>특정 SlackRecord soft delete</p>
     *
     * @param slackRecordId 식별자
     * @param loginUserId    권한 확인을 위한 사용자 ID
     * @return {@link SlackRecordDeleteDto}
     */
    @Transactional
    public SlackRecordDeleteDto softDeleteSlackRecord(UUID slackRecordId, UUID loginUserId) {
        SlackRecord slackRecord = getRecord(slackRecordId);
        slackRecord.softDelete(loginUserId);
        return SlackRecordDeleteDto.from(slackRecord);
    }




    private SlackRecord getRecord(UUID slackRecordId) {
        return repository.findById(slackRecordId).orElseThrow(SlackRecordNotFoundException::new);
    }

    private Pageable createPageable(int page, int size, String sortBy, boolean isAsc) {
        int validatedSize = List.of(10, 30, 50).contains(size) ? size : 10;
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page - 1, validatedSize, Sort.by(direction, sortBy));
    }
}
