package hmw.ecommerce.controller;


import hmw.ecommerce.entity.dto.Item.ItemMainViewDto;
import hmw.ecommerce.entity.dto.Item.ItemRegisterDto;
import hmw.ecommerce.entity.dto.Item.ItemUpdateForm;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/item")
public class ItemController {

    private final ItemService itemService;

    /**
     * 새로운 아이템을 등록
     *
     * @param token 인증을 위한 JWT 토큰
     * @param itemRegisterDto 아이템 등록 정보를 담고 있는 DTO
     * @param bindingResult 유효성 검사 결과
     * @return 등록된 아이템 정보
     */
    @PostMapping
    public ResponseEntity<?> register(
            @RequestHeader(name = Const.AUTHORIZATION) String token,
            @Valid @RequestBody ItemRegisterDto.Request itemRegisterDto,
            BindingResult bindingResult) {

        return ResponseEntity.ok(itemService.register(itemRegisterDto, token));
    }

    /**
     * 메인 페이지에 표시할 아이템 목록을 조회합니다.
     *
     * @return 메인 페이지에 표시할 아이템 목록
     */
    @GetMapping()
    public ResponseEntity<?> getItemMainPage() {
        ItemMainViewDto itemMainPage = itemService.getItemMainPage();
        return ResponseEntity.ok(itemMainPage);
    }

    /**
     * 주어진 아이템 ID에 대한 상세 정보를 조회합니다.
     *
     * @param itemId 조회할 아이템 ID
     * @return 아이템 상세 정보
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<?> getItemDetail(@PathVariable(name = "itemId") Long itemId) {
        return ResponseEntity.ok(itemService.getItemDetail(itemId));
    }

    /**
     * 아이템을 삭제합니다.
     *
     * @param itemId 삭제할 아이템 ID
     * @param token 인증을 위한 JWT 토큰
     * @return 삭제된 아이템 정보
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable(name = "itemId") Long itemId,
                                        @RequestHeader(Const.AUTHORIZATION) String token) {
        return ResponseEntity.ok(itemService.removeItem(itemId, token));
    }

    /**
     * 아이템 정보를 수정합니다.
     *
     * @param token 인증을 위한 JWT 토큰
     * @param itemId 수정할 아이템 ID
     * @param updateForm 수정할 아이템 정보를 담고 있는 DTO
     * @param bindingResult 유효성 검사 결과
     * @return 수정된 아이템 정보
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateItem(
            @RequestHeader(Const.AUTHORIZATION) String token,
            @PathVariable(name = "itemId") Long itemId,
            @Valid @RequestBody ItemUpdateForm updateForm,
            BindingResult bindingResult
            ) {
        return ResponseEntity.ok(itemService.modifyItem(token, itemId, updateForm));
    }

    /**
     * 카테고리, 타입으로 아이템을 검색하고 페이지네이션을 적용합니다.
     *
     * @param category 아이템의 카테고리
     * @param type 아이템의 타입
     * @param page 요청할 페이지 번호
     * @param size 페이지당 아이템 개수
     * @return 검색된 아이템 목록
     */
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
