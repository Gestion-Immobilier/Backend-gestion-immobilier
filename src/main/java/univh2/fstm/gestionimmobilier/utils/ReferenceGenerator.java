package univh2.fstm.gestionimmobilier.utils;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import univh2.fstm.gestionimmobilier.model.TypeBien;
import univh2.fstm.gestionimmobilier.repository.BienRepository;

import java.time.Year;


@Component
@RequiredArgsConstructor
public class ReferenceGenerator {

    private final BienRepository bienRepository;

    public String genererReferenceBien(TypeBien typeBien) {
        String typeCode = getTypeCode(typeBien);
        int annee = Year.now().getValue();

        int numero = 1;
        String reference;
        do {
            reference = String.format("BIEN-%s-%d-%03d", typeCode, annee, numero);
            numero++;
        } while (bienRepository.existsByReference(reference));

        return reference;
    }

    private String getTypeCode(TypeBien typeBien) {
        return switch (typeBien) {
            case APPARTEMENT -> "APPT";
            case MAISON -> "MAIS";
            case STUDIO -> "STUD";
            case COMMERCE -> "COMM";
            case BUREAU -> "BURO";
            case GARAGE -> "GARA";
            case TERRAIN -> "TERR";
        };
}}