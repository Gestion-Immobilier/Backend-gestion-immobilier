package univh2.fstm.gestionimmobilier.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import univh2.fstm.gestionimmobilier.dto.request.ContratRequestDto;
import univh2.fstm.gestionimmobilier.exception.BadRequestException;
import univh2.fstm.gestionimmobilier.exception.ResourceNotFoundException;
import univh2.fstm.gestionimmobilier.model.Bien;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.model.StatutBien;
import univh2.fstm.gestionimmobilier.model.StatutValidation;
import univh2.fstm.gestionimmobilier.model.Type;
import univh2.fstm.gestionimmobilier.repository.BienRepository;
import univh2.fstm.gestionimmobilier.repository.ContratRepository;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;
import univh2.fstm.gestionimmobilier.service.impl.ContratServiceImpl;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContratServiceImplTest {

    @Mock
    private ContratRepository contratRepository;

    @Mock
    private BienRepository bienRepository;

    @Mock
    private PersonneRepository personneRepository;

    @InjectMocks
    private ContratServiceImpl contratService;

    private ContratRequestDto requestDto;
    private Bien bien;
    private Personne locataire;
    private MultipartFile mockPdf;

    @BeforeEach
    void setUp() {
        requestDto = new ContratRequestDto();
        requestDto.setBienId(1L);
        requestDto.setLocataireId(2L);
        requestDto.setDateDebut(LocalDate.now());
        requestDto.setDateFin(LocalDate.now().plusYears(1));

        bien = new Bien();
        bien.setId(1L);
        bien.setStatut(StatutBien.DISPONIBLE);
        bien.setStatutValidation(StatutValidation.VALIDE);

        locataire = new Personne();
        locataire.setId(2L);
        locataire.setType(Type.LOCATAIRE);

        mockPdf = mock(MultipartFile.class);
    }

    @Test
    void creerContrat_DateFinAvantDebut_DevraitLeverException() {
        when(bienRepository.findById(1L)).thenReturn(Optional.of(bien));
        when(personneRepository.findById(2L)).thenReturn(Optional.of(locataire));
        
        requestDto.setDateFin(LocalDate.now().minusDays(1));

        assertThrows(BadRequestException.class, () -> contratService.creerContrat(requestDto, mockPdf));
    }

    @Test
    void creerContrat_SansPdf_DevraitLeverException() {
        when(bienRepository.findById(1L)).thenReturn(Optional.of(bien));
        when(personneRepository.findById(2L)).thenReturn(Optional.of(locataire));
        
        when(mockPdf.isEmpty()).thenReturn(true);

        assertThrows(BadRequestException.class, () -> contratService.creerContrat(requestDto, mockPdf));
    }

    @Test
    void creerContrat_AvecPersonneNonLocataire_DevraitLeverException() {
        when(bienRepository.findById(1L)).thenReturn(Optional.of(bien));
        
        locataire.setType(Type.PROPRIETAIRE);
        when(personneRepository.findById(2L)).thenReturn(Optional.of(locataire));

        assertThrows(BadRequestException.class, () -> contratService.creerContrat(requestDto, mockPdf));
    }

    @Test
    void creerContrat_BienInexistant_DevraitLeverNotFoundException() {
        when(bienRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contratService.creerContrat(requestDto, mockPdf));
    }
}
