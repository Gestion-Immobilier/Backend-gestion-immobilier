package univh2.fstm.gestionimmobilier.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import univh2.fstm.gestionimmobilier.dto.request.BienRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.BienResponseDto;
import univh2.fstm.gestionimmobilier.exception.BadRequestException;
import univh2.fstm.gestionimmobilier.exception.ResourceNotFoundException;
import univh2.fstm.gestionimmobilier.mapper.BienMapper;
import univh2.fstm.gestionimmobilier.model.Bien;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.model.Type;
import univh2.fstm.gestionimmobilier.repository.BienRepository;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;
import univh2.fstm.gestionimmobilier.service.MinioService;
import univh2.fstm.gestionimmobilier.service.impl.BienServiceImpl;
import univh2.fstm.gestionimmobilier.utils.ReferenceGenerator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BienServiceImplTest {

    @Mock
    private BienRepository bienRepository;

    @Mock
    private PersonneRepository personneRepository;

    @Mock
    private BienMapper bienMapper;

    @Mock
    private ReferenceGenerator referenceGenerator;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private BienServiceImpl bienService;

    private Personne proprietaire;
    private BienRequestDto bienRequestDto;
    private Bien bien;

    @BeforeEach
    void setUp() {
        proprietaire = new Personne();
        proprietaire.setId(1L);
        proprietaire.setType(Type.PROPRIETAIRE);
        proprietaire.setVerified(true);

        bienRequestDto = new BienRequestDto();
        bienRequestDto.setProprietaireId(1L);

        bien = new Bien();
        bien.setId(10L);
    }

    @Test
    void creerBien_avecProprietaireValide_DevraitReussir() {
        when(personneRepository.findById(1L)).thenReturn(Optional.of(proprietaire));
        when(bienMapper.toEntity(bienRequestDto)).thenReturn(bien);
        when(referenceGenerator.genererReferenceBien(any())).thenReturn("REF-123");
        when(bienRepository.save(any(Bien.class))).thenReturn(bien);
        when(bienMapper.toResponseDto(bien)).thenReturn(new BienResponseDto());

        BienResponseDto response = bienService.creerBien(bienRequestDto, null);

        assertNotNull(response);
        verify(bienRepository, times(1)).save(any(Bien.class));
    }

    @Test
    void creerBien_avecProprietaireNonVerifie_DevraitLeverException() {
        proprietaire.setVerified(false);
        when(personneRepository.findById(1L)).thenReturn(Optional.of(proprietaire));

        assertThrows(BadRequestException.class, () -> bienService.creerBien(bienRequestDto, null));
        verify(bienRepository, never()).save(any(Bien.class));
    }

    @Test
    void creerBien_avecPersonneNonProprietaire_DevraitLeverException() {
        proprietaire.setType(Type.LOCATAIRE);
        when(personneRepository.findById(1L)).thenReturn(Optional.of(proprietaire));

        assertThrows(BadRequestException.class, () -> bienService.creerBien(bienRequestDto, null));
        verify(bienRepository, never()).save(any(Bien.class));
    }

    @Test
    void getBienById_QuandInexistant_DevraitLeverResourceNotFoundException() {
        when(bienRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bienService.getBienById(99L));
    }
}
