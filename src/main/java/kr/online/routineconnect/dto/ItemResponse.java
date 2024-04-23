package kr.online.routineconnect.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ItemResponse {
    private Long itemId;
    private Long itemOrderId;
    private String hour;
    private String title;
    private Double position;
    private Boolean accomplishment;
    private String retrospect;

    @Builder
    public ItemResponse(Long itemId, Long itemOrderId, String hour, String title, Double position,
                        Boolean accomplishment,
                        String retrospect) {
        this.itemId = itemId;
        this.itemOrderId = itemOrderId;
        this.hour = hour;
        this.title = title;
        this.position = position;
        this.accomplishment = accomplishment;
        this.retrospect = retrospect;
    }
}
