package hmw.ecommerce.entity.dto.Item;

import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@Builder
public class ItemThumbnailResponseDto implements Serializable {

    private Long itemId;
    private String title;

    private Long memberId;
    private String loginId;
    private String username;
    private String nickName;

    public static ItemThumbnailResponseDto fromItemEntity(Item item, Member member) {
        return ItemThumbnailResponseDto.builder()
                .itemId(item.getId())
                .title(item.getTitle())
                .memberId(member.getId())
                .loginId(member.getLoginId())
                .username(member.getUsername())
                .nickName(member.getNickName())
                .build();
    }

}