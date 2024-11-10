package hmw.ecommerce.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class MainItemViewDto {

    Map<Long, ItemThumbnailResponseDto> top15ItemsMap;
    List<ItemThumbnailResponseDto> recentItems;

}
