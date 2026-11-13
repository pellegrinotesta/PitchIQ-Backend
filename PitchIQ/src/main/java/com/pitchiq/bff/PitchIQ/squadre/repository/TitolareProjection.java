package com.pitchiq.bff.PitchIQ.squadre.repository;

public interface TitolareProjection {
    Long getId();
    String getNome();
    String getCognome();
    String getPosizione();
    Integer getNumeroMaglia();
    String getModuloFonte();
}
