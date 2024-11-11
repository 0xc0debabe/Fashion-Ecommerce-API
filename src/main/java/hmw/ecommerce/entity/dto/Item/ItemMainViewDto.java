package hmw.ecommerce.entity.dto.Item;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class ItemMainViewDto {

    Map<Long, ItemThumbnailResponseDto> top15ItemsMap;
    List<ItemThumbnailResponseDto> recentItems;

}
