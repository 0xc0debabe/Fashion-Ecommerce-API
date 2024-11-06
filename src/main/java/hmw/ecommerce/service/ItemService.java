package hmw.ecommerce.service;

import hmw.ecommerce.entity.Category;
import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.dto.ItemRegisterDto;
import hmw.ecommerce.repository.CategoryRepository;
import hmw.ecommerce.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@RequiredArgsConstructor
@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public ItemRegisterDto.Response registerItem(ItemRegisterDto.Request itemRegisterDto) {

        Item item = ItemRegisterDto.Request.toItemEntity(itemRegisterDto);
        itemRepository.save(item);

        Set<String> categoryNames = itemRegisterDto.getCategoryNames();
        for (String categoryName : categoryNames) {
            Category category = ItemRegisterDto.Request.toCategoryEntity(categoryName);
            category.addItemCategories(item, category);
            categoryRepository.save(category);
        }

        return new ItemRegisterDto.Response(item.getItemName(), item.getPrice(), item.getStockQuantity(), categoryNames);
    }
}