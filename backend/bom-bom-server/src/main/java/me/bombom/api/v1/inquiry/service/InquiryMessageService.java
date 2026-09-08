package me.bombom.api.v1.inquiry.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import me.bombom.api.v1.inquiry.domain.InquiryMessageImage;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.dto.InquiryMessageResponse;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.SendInquiryMessageRequest;
import me.bombom.api.v1.inquiry.repository.InquiryMessageImageRepository;
import me.bombom.api.v1.inquiry.repository.InquiryMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryMessageService {

    private final InquiryMessageRepository inquiryMessageRepository;
    private final InquiryMessageImageRepository inquiryMessageImageRepository;
    private final InquiryRoomService inquiryRoomService;

    @Transactional
    public InquiryMessageResponse sendMessage(InquiryRequester requester, Long roomId, SendInquiryMessageRequest request) {
        InquiryRoom room = inquiryRoomService.getOwnedRoom(roomId, requester);

        InquiryMessage message = inquiryMessageRepository.save(InquiryMessage.createUserMessage(room.getId(), request.content()));
        List<InquiryMessageImage> images = saveImages(message.getId(), request.imageUrls());

        return InquiryMessageResponse.of(message, images);
    }

    private List<InquiryMessageImage> saveImages(Long messageId, List<String> imageUrls) {
        if (CollectionUtils.isEmpty(imageUrls)) {
            return List.of();
        }

        List<InquiryMessageImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            images.add(inquiryMessageImageRepository.save(new InquiryMessageImage(messageId, imageUrls.get(i), i)));
        }
        return images;
    }
}
