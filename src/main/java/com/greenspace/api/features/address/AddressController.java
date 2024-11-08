package com.greenspace.api.features.address;

import java.util.UUID;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenspace.api.dto.address.AddressDTO;
import com.greenspace.api.dto.responses.Response;

@RestController
@RequestMapping("/api/address")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/create")
    public ResponseEntity<Response<Object>> create(@RequestBody AddressDTO addressDto) {
        Response<Object> response = Response.builder()
                .data(addressService.create(addressDto))
                .status(201)
                .build();

        return ResponseEntity.status(HttpStatusCode.valueOf(201)).body(response);
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<Response<Object>> update(@PathVariable UUID id, @RequestBody AddressDTO addressDto) {
        Response<Object> response = Response.builder()
                .data(addressService.update(id, addressDto))
                .status(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getLoggedUserAddress")
    public ResponseEntity<Response<Object>> getLoggedUserAddress() {
        Response<Object> response = Response.builder()
                .data(addressService.getLoggedUserAddress())
                .status(200)
                .build();

        return ResponseEntity.ok(response);
    }

}
