package hmw.ecommerce.controller;


import hmw.ecommerce.entity.dto.ItemRegisterDto;
import hmw.ecommerce.entity.dto.MainItemViewDto;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

}
