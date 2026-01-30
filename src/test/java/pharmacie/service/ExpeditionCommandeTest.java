package pharmacie.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import pharmacie.dao.DispensaireRepository;
import pharmacie.dao.LigneRepository;
import pharmacie.dao.MedicamentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
// Ce test est basé sur le jeu de données dans "test_data.sql"
class ExpeditionCommandeTest {
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
    void testEnregistreExpedition() {
        // Créer une commande et ajouter une ligne
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        int commandeNum = commande.getNumero();
        int medicamentRef = 93;
        int quantite = 10;

        var ligne = service.ajouterLigne(commandeNum, medicamentRef, quantite);

        var medicamentAvant = daoMedicament.findById(medicamentRef).orElseThrow();
        int unitesEnStockAvant = medicamentAvant.getUnitesEnStock();
        int unitesCommandeesAvant = medicamentAvant.getUnitesCommandees();

        var commandeExpediee = service.enregistreExpedition(commandeNum);

        assertNotNull(commandeExpediee.getEnvoyeele(), "La date d'expédition doit être définie");
        assertEquals(LocalDate.now(), commandeExpediee.getEnvoyeele(), "La date d'expédition doit être aujourd'hui");

        var medicamentApres = daoMedicament.findById(medicamentRef).orElseThrow();
        assertEquals(unitesEnStockAvant - quantite, medicamentApres.getUnitesEnStock(),
                "Les unités en stock doivent être décrémentées de la quantité expédiée");
        assertEquals(unitesCommandeesAvant - quantite, medicamentApres.getUnitesCommandees(),
                "Les unités commandées doivent être décrémentées de la quantité expédiée");
    }

    @Test
    void testEnregistreExpeditionCommandeInexistante() {
        assertThrows(NoSuchElementException.class, () -> service.enregistreExpedition(999999),
                "Doit lancer NoSuchElementException si la commande n'existe pas");
    }

    @Test
    void testEnregistreExpeditionCommandeDejaEnvoyee() {
        assertThrows(IllegalStateException.class, () -> service.enregistreExpedition(99999),
                "Doit lancer IllegalStateException si la commande est déjà envoyée");
    }

}