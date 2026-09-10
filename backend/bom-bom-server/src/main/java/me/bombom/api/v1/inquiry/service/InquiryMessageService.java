package me.bombom.api.v1.inquiry.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import me.bombom.api.v1.inquiry.domain.InquiryMessageImage;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.request.SendInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryMessagePageResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryMessageResponse;
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
        validateContentOrImages(request);
        InquiryRoom room = inquiryRoomService.getOwnedRoom(roomId, requester);

        InquiryMessage message = inquiryMessageRepository.save(InquiryMessage.createUserMessage(room.getId(), request.content()));
        List<InquiryMessageImage> images = saveImages(message.getId(), request.imageUrls());

        return InquiryMessageResponse.of(message, images);
    }

    public InquiryMessagePageResponse getMessages(InquiryRequester requester, Long roomId, Long cursor, int size) {
        inquiryRoomService.getOwnedRoom(roomId, requester);

        List<InquiryMessage> messages = inquiryMessageRepository.findMessagesByCursor(roomId, cursor, size + 1);
        boolean hasNext = messages.size() > size;
        List<InquiryMessage> pageMessages = hasNext ? messages.subList(0, size) : messages;

        List<Long> messageIds = pageMessages.stream().map(InquiryMessage::getId).toList();
        List<InquiryMessageImage> images = inquiryMessageImageRepository.findByMessageIdInOrderBySortOrderAsc(messageIds);
        Map<Long, List<InquiryMessageImage>> imagesByMessageId = images.stream()
                .collect(Collectors.groupingBy(InquiryMessageImage::getMessageId));

        return InquiryMessagePageResponse.of(pageMessages, imagesByMessageId, hasNext);
    }

    @Transactional
    public InquiryMessageResponse updateMessage(
            InquiryRequester requester, Long roomId, Long messageId, UpdateInquiryMessageRequest request
    ) {
        inquiryRoomService.getOwnedRoom(roomId, requester);
        InquiryMessage message = getMessageInRoom(roomId, messageId);
        validateWrittenByUser(message);

        message.updateContent(request.content());
        List<InquiryMessageImage> images = inquiryMessageImageRepository.findByMessageIdInOrderBySortOrderAsc(List.of(messageId));
        return InquiryMessageResponse.of(message, images);
    }

    @Transactional
    public void deleteMessage(InquiryRequester requester, Long roomId, Long messageId) {
        inquiryRoomService.getOwnedRoom(roomId, requester);
        InquiryMessage message = getMessageInRoom(roomId, messageId);
        validateWrittenByUser(message);

        inquiryMessageImageRepository.deleteByMessageId(messageId);
        inquiryMessageRepository.delete(message);
    }

    private void validateContentOrImages(SendInquiryMessageRequest request) {
        boolean hasContent = request.content() != null && !request.content().isEmpty();
        boolean hasImages = !CollectionUtils.isEmpty(request.imageUrls());
        if (!hasContent && !hasImages) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext("reason", "content_or_images_required");
        }
    }

    private InquiryMessage getMessageInRoom(Long roomId, Long messageId) {
        InquiryMessage message = inquiryMessageRepository.findById(messageId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext("messageId", messageId));

        if (!message.getRoomId().equals(roomId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("messageId", messageId)
                    .addContext("roomId", roomId);
        }
        return message;
    }

    private void validateWrittenByUser(InquiryMessage message) {
        if (!message.isWrittenByUser()) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                    .addContext("messageId", message.getId());
        }
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
