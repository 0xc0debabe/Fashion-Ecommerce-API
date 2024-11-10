package hmw.ecommerce.controller;


import hmw.ecommerce.entity.dto.ItemRegisterDto;
import hmw.ecommerce.entity.dto.ItemUpdateForm;
import hmw.ecommerce.entity.dto.MainItemViewDto;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/item")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<?> register(
            @RequestHeader(name = Const.AUTHORIZATION) String token,
            @Valid @RequestBody ItemRegisterDto.Request itemRegisterDto,
            BindingResult bindingResult) {

        return ResponseEntity.ok(itemService.register(itemRegisterDto, token));
    }

    @GetMapping()
    public ResponseEntity<?> getItemMainPage() {
        MainItemViewDto itemMainPage = itemService.getItemMainPage();
        return ResponseEntity.ok(itemMainPage);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<?> getItemDetail(@PathVariable(name = "itemId") Long itemId) {
        return ResponseEntity.ok(itemService.getItemDetail(itemId));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable(name = "itemId") Long itemId,
                                        @RequestHeader(Const.AUTHORIZATION) String token) {
        return ResponseEntity.ok(itemService.removeItem(itemId, token));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateItem(
            @RequestHeader(Const.AUTHORIZATION) String token,
            @PathVariable(name = "itemId") Long itemId,
            @Valid @RequestBody ItemUpdateForm updateForm,
            BindingResult bindingResult
            ) {
        return ResponseEntity.ok(itemService.modifyItem(token, itemId, updateForm));
    }

    @GetMapping("/category")
    public ResponseEntity<?> searchItemByCategory(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(itemService.searchItemByCategory(category, type, pageable));
    }

}
