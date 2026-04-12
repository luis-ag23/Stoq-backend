package com.Proyecto.stoq.security;

import java.util.List;

public final class RoleCatalog {

    public static final String ADMIN = "ADMIN";
    public static final String OPERADOR = "OPERADOR";
    public static final String GERENTE = "GERENTE";

    private RoleCatalog() {
    }

    public static String normalize(String role) {
        if (role == null) {
            return null;
        }

        String normalized = role.trim().toUpperCase();
        if ("USER".equals(normalized)) {
            return OPERADOR;
        }

        return normalized;
    }

    public static boolean isAllowed(String role) {
        String normalized = normalize(role);
        return ADMIN.equals(normalized) || OPERADOR.equals(normalized) || GERENTE.equals(normalized);
    }

    public static List<String> permissionsFor(String role) {
        String normalized = normalize(role);

        return switch (normalized) {
            case ADMIN -> List.of(
                    "Gestionar usuarios",
                    "Gestionar roles y permisos",
                    "Configurar categorias y unidades",
                    "Definir stock minimo",
                    "Ver auditorias globales",
                    "Acceso total al inventario"
            );
            case OPERADOR -> List.of(
                    "Registrar entradas y salidas",
                    "Consultar stock y ubicacion",
                    "Usar validaciones de seguridad"
            );
            case GERENTE -> List.of(
                    "Ver stock critico",
                    "Consultar top salidas",
                    "Revisar rotacion y tendencias",
                    "Filtrar reportes por fecha",
                    "Exportar reportes"
            );
            default -> List.of();
        };
    }
}