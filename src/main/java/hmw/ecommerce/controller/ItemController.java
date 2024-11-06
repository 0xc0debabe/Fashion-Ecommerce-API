package hmw.ecommerce.controller;


import hmw.ecommerce.entity.dto.ItemRegisterDto;
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
    public ResponseEntity<?> registerItem(
            @Valid @RequestBody ItemRegisterDto.Request itemRegisterDto,
            BindingResult bindingResult) {
        return ResponseEntity.ok(itemService.registerItem(itemRegisterDto));
    }

}
