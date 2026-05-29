package org.example.generator;

import org.example.model.*;

public class SmartSentenceGenerator {

    public String describePage(PageInfo pageInfo) {

        String title = pageInfo.getPageTitle().toLowerCase();

        if (title.contains("statistik")) {
            return "Faqja përdoret për konsultimin dhe analizimin e të dhënave statistikore.";
        }

        if (title.contains("aplikim")) {
            return "Faqja mundëson kërkimin, filtrimin dhe konsultimin e aplikimeve të regjistruara në sistem.";
        }

        if (title.contains("përdorues")
                || title.contains("user")) {

            return "Faqja përdoret për administrimin e përdoruesve dhe të drejtave të aksesit.";
        }

        return "Faqja përdoret për menaxhimin e informacionit dhe funksionaliteteve përkatëse të sistemit.";
    }
}