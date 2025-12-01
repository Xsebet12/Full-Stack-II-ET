package com.SebastianCornejo.Proyecto.Fullstack.dto;

public class SolicitudVentaOpcional {
    private String metodoPago;
    private String canal;
    private java.util.List<Item> items;

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public java.util.List<Item> getItems() { return items; }
    public void setItems(java.util.List<Item> items) { this.items = items; }

    public static class Item {
        private Long productoId;
        private Integer cantidad;

        public Long getProductoId() { return productoId; }
        public void setProductoId(Long productoId) { this.productoId = productoId; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }
}
