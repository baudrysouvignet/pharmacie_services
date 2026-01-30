package pharmacie.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import pharmacie.dao.DispensaireRepository;
import pharmacie.dao.LigneRepository;
import pharmacie.dao.MedicamentRepository;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
// Ce test est basé sur le jeu de données dans "test_data.sql"
class SupprimerLigneTest {
    private static final String ID_PETIT_CLIENT = "0COM";
    private static final String ID_GROS_CLIENT = "2COM";
    private static final BigDecimal REMISE_POUR_GROS_CLIENT = new BigDecimal("0.15");

    @Autowired
    private CommandeService service;
    @Autowired
    private DispensaireRepository daoClient;
    @Autowired
    private LigneRepository ligneDao;
    @Autowired
    private MedicamentRepository daoMedicament;

    @Test
    void testSupprimerLigne() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        int commandeNum = commande.getNumero();
        int medicamentRef = 93;
        int quantite = 10;

        var ligne = service.ajouterLigne(commandeNum, medicamentRef, quantite);
        int ligneId = ligne.getId();

        var medicamentAvant = daoMedicament.findById(medicamentRef).orElseThrow();
        int unitesCommandeesAvant = medicamentAvant.getUnitesCommandees();

        service.supprimerLigne(ligneId);

        var medicamentApres = daoMedicament.findById(medicamentRef).orElseThrow();
        assertEquals(unitesCommandeesAvant - quantite, medicamentApres.getUnitesCommandees(),
                "Les unités commandées doivent être décrémentées de la quantité supprimée");

        assertThrows(NoSuchElementException.class, () -> ligneDao.findById(ligneId).orElseThrow(),
                "La ligne doit être supprimée");
    }

    @Test
    void testSupprimerLigneCommandeDejaEnvoyee() {
        var lignesEnvoyees = ligneDao.findByCommandeNumero(99999);
        if (!lignesEnvoyees.isEmpty()) {
            int ligneId = lignesEnvoyees.get(0).getId();
            assertThrows(IllegalStateException.class, () -> service.supprimerLigne(ligneId),
                    "Doit lancer IllegalStateException si la commande est déjà envoyée");
        }
    }

    @Test
    void testSupprimerLigneInexistante() {
        assertThrows(NoSuchElementException.class, () -> service.supprimerLigne(999999),
                "Doit lancer NoSuchElementException si la ligne n'existe pas");
    }

}
