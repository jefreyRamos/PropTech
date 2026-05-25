package proptech.util;

import proptech.modelo.Asesor;
import proptech.modelo.Cliente;
import proptech.modelo.Inmueble;
import proptech.modelo.Visita;
import proptech.servicios.PropTechService;

/** Carga datos de demostración si la BD está vacía. */
public class DatosDePrueba {

    public static void cargarSiEsNecesario(PropTechService s) {
        if (s.countInmuebles() > 0) return;
        System.out.println("[Demo] Cargando datos de prueba...");

        s.registrarAsesor(new Asesor("AS-001","Laura Gómez","310-111-2222","Residencial","Norte"));
        s.registrarAsesor(new Asesor("AS-002","Carlos Rueda","320-333-4444","Comercial","Centro"));
        s.registrarAsesor(new Asesor("AS-003","Sofía Vargas","315-555-6666","Premium","Occidente"));

        s.registrarInmueble(new Inmueble("INM-001","Cra 15 #45-20","Bogotá","Chapinero",
                Inmueble.Tipo.APARTAMENTO,Inmueble.Finalidad.ARRIENDO,1_800_000,65,2,1,Inmueble.Estado.DISPONIBLE,"AS-001"));
        s.registrarInmueble(new Inmueble("INM-002","Cl 80 #12-10","Bogotá","Suba",
                Inmueble.Tipo.CASA,Inmueble.Finalidad.VENTA,350_000_000,120,3,2,Inmueble.Estado.DISPONIBLE,"AS-001"));
        s.registrarInmueble(new Inmueble("INM-003","Av 68 #25-30","Bogotá","Engativá",
                Inmueble.Tipo.APARTAMENTO,Inmueble.Finalidad.ARRIENDO,1_200_000,55,2,1,Inmueble.Estado.DISPONIBLE,"AS-001"));
        s.registrarInmueble(new Inmueble("INM-004","Cl 19 #5-40","Bogotá","La Candelaria",
                Inmueble.Tipo.LOCAL_COMERCIAL,Inmueble.Finalidad.ARRIENDO,2_500_000,80,0,1,Inmueble.Estado.DISPONIBLE,"AS-002"));
        s.registrarInmueble(new Inmueble("INM-005","Cra 7 #100-15","Bogotá","Usaquén",
                Inmueble.Tipo.APARTAMENTO,Inmueble.Finalidad.VENTA,420_000_000,95,3,2,Inmueble.Estado.DISPONIBLE,"AS-003"));
        s.registrarInmueble(new Inmueble("INM-006","Cl 50 #30-10","Medellín","El Poblado",
                Inmueble.Tipo.APARTAMENTO,Inmueble.Finalidad.ARRIENDO,2_200_000,75,2,2,Inmueble.Estado.DISPONIBLE,"AS-003"));
        s.registrarInmueble(new Inmueble("INM-007","Cra 43 #18-50","Medellín","Laureles",
                Inmueble.Tipo.CASA,Inmueble.Finalidad.VENTA,280_000_000,145,4,3,Inmueble.Estado.DISPONIBLE,"AS-002"));
        s.registrarInmueble(new Inmueble("INM-008","Cl 10 #85-20","Cali","Granada",
                Inmueble.Tipo.OFICINA,Inmueble.Finalidad.ARRIENDO,1_900_000,60,0,1,Inmueble.Estado.DISPONIBLE,"AS-002"));
        s.registrarInmueble(new Inmueble("INM-009","Av 9N #20-10","Cali","San Fernando",
                Inmueble.Tipo.APARTAMENTO,Inmueble.Finalidad.VENTA,195_000_000,70,2,2,Inmueble.Estado.DISPONIBLE,"AS-001"));
        s.registrarInmueble(new Inmueble("INM-010","Cl 72 #50-30","Bogotá","Barrios Unidos",
                Inmueble.Tipo.BODEGA,Inmueble.Finalidad.ARRIENDO,3_500_000,200,0,1,Inmueble.Estado.DISPONIBLE,"AS-002"));

        s.registrarCliente(new Cliente("12345678","Andrés Morales","andres@mail.com","315-100-2000",
                Cliente.Tipo.ARRENDATARIO,2_000_000,"Chapinero","APARTAMENTO",2));
        s.registrarCliente(new Cliente("87654321","María Rodríguez","maria@mail.com","317-200-3000",
                Cliente.Tipo.COMPRADOR,400_000_000,"Usaquén","CASA",3));
        s.registrarCliente(new Cliente("11223344","Juan Pérez","juan@mail.com","318-400-5000",
                Cliente.Tipo.INVERSOR,500_000_000,"El Poblado","APARTAMENTO",2));
        s.registrarCliente(new Cliente("44332211","Paula Sánchez","paula@mail.com","314-600-7000",
                Cliente.Tipo.VIP,450_000_000,"Usaquén","APARTAMENTO",3));
        s.registrarCliente(new Cliente("55667788","Ricardo Torres","ricardo@mail.com","312-800-9000",
                Cliente.Tipo.COMPRADOR,300_000_000,"Laureles","CASA",3));

        s.agendarVisita(new Visita("12345678","INM-001","2025-06-15 10:00","AS-001",Visita.Prioridad.NORMAL));
        s.agendarVisita(new Visita("87654321","INM-002","2025-06-16 14:00","AS-001",Visita.Prioridad.ALTA));
        s.agendarVisita(new Visita("11223344","INM-005","2025-06-17 09:00","AS-003",Visita.Prioridad.VIP));
        s.agendarVisita(new Visita("44332211","INM-005","2025-06-18 11:00","AS-003",Visita.Prioridad.VIP));
        s.agendarVisita(new Visita("55667788","INM-007","2025-06-19 15:00","AS-002",Visita.Prioridad.NORMAL));

        System.out.println("[Demo] Listo: 3 asesores | 10 inmuebles | 5 clientes | 5 visitas");
    }
}