package com.greenspace.api.features.address;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.greenspace.api.dto.address.AddressDTO;
import com.greenspace.api.error.http.BadRequest400Exception;
import com.greenspace.api.error.http.NotFound404Exception;
import com.greenspace.api.features.user.UserRepository;
import com.greenspace.api.jwt.Jwt;
import com.greenspace.api.models.AddressModel;
import com.greenspace.api.models.UserModel;
import com.greenspace.api.utils.Validation;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final Jwt jwt;
    private final UserRepository userRepository;
    private final Validation validationUtil;

    public AddressService(
            AddressRepository addressRepository,
            Jwt jwt,
            UserRepository userRepository,
            Validation validation) {
        this.addressRepository = addressRepository;
        this.jwt = jwt;
        this.userRepository = userRepository;
        this.validationUtil = validation;
    }

    AddressModel create(AddressDTO addressDto) {
        // Pega o email do usuário logado
        String loggedUserEmail = jwt.getCurrentUserEmail();

        // Verifica se o usuario ja tem um endereço cadastrado
        if (userRepository.findAddressByEmailAddress(loggedUserEmail).isPresent()) {
            throw new BadRequest400Exception("User with email " + loggedUserEmail + " already has an address");
        }

        if (!isValidBrazilianZipCode(addressDto.getPostalCode())) {
            throw new BadRequest400Exception("Invalid Brazilian zip code syntax, it should be XXXXX-XXX received "
                    + addressDto.getPostalCode());
        }

        // Cria uma nova entidade de endereço com os dados do DTO
        AddressModel address = AddressModel.builder()
                .city(addressDto.getCity())
                .country(addressDto.getCountry())
                .postalCode(addressDto.getPostalCode())
                .state(addressDto.getState())
                .street(addressDto.getStreet())
                .isValidated(false)
                .build();

        // Pega a entidade do usuário logado
        UserModel loggedUserEntity = userRepository.findByEmailAddress(loggedUserEmail).orElseThrow(
                () -> new NotFound404Exception(
                        "User not found with email " + loggedUserEmail + ", perhaps you're not logged in?"));

        // Salva o endereço no banco de dados, fazendo ele ganhar um UUID unico
        AddressModel createdAddress = addressRepository.save(address);

        // Adiciona o endereço ao usuário logado e salva o usuario no banco de dados
        loggedUserEntity.setAddress(createdAddress);
        userRepository.save(loggedUserEntity);

        return createdAddress;
    }

    public AddressModel update(UUID id, AddressDTO addressDto) {

        AddressModel existingAdress = addressRepository.findById(id)
                .orElseThrow(() -> new NotFound404Exception("Address not found with id " + id));

        if (!isValidBrazilianZipCode(addressDto.getPostalCode())) {
            throw new BadRequest400Exception("Invalid Brazilian zip code syntax, it should be XXXXX-XXX received "
                    + addressDto.getPostalCode());
        }

        // Compara os campos do endereço existente com os campos do novo endereço para
        // ver o que precisa atualizar
        if (addressDto.getCity() != null && !addressDto.getCity().equals(existingAdress.getCity())) {
            existingAdress.setCity(addressDto.getCity());
        }
        if (addressDto.getCountry() != null && !addressDto.getCountry().equals(existingAdress.getCountry())) {
            existingAdress.setCountry(addressDto.getCountry());
        }
        if (addressDto.getPostalCode() != null && !addressDto.getPostalCode().equals(existingAdress.getPostalCode())) {
            existingAdress.setPostalCode(addressDto.getPostalCode());
        }
        if (addressDto.getState() != null && !addressDto.getState().equals(existingAdress.getState())) {
            existingAdress.setState(addressDto.getState());
        }
        if (addressDto.getStreet() != null && !addressDto.getStreet().equals(existingAdress.getStreet())) {
            existingAdress.setStreet(addressDto.getStreet());
        }

        return addressRepository.save(existingAdress);
    }

    public void softdeleteLoggedUserAddress() {
        String userEmail = jwt.getCurrentUserEmail();

        AddressModel address = userRepository.findAddressByEmailAddress(userEmail).orElseThrow(
                () -> new NotFound404Exception("Address not found for user with email " + userEmail
                        + ", may the user never registered an address"));

        address.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));

        addressRepository.save(address);
    }

    public void restoreSoftdeletedUserAddress(UserModel user) {
        AddressModel address = user.getAddress();
        address.setDeletedAt(null);

        addressRepository.save(address);
    }

    public AddressModel getLoggedUserAddress() {
        String userEmail = jwt.getCurrentUserEmail();

        return userRepository.findAddressByEmailAddress(userEmail).orElseThrow(
                () -> new NotFound404Exception("Address not found for user with email " + userEmail
                        + ", may the user never registered an address"));
    }

    public boolean isValidBrazilianZipCode(String zipCode) {
        return validationUtil.isFieldValid(
                zipCode,
                validationUtil.BRAZILIAN_ZIPCODE_PATTERN);
    }

}
