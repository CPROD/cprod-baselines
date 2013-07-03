package baseline;

public class Product {

    private String id;
    private String description;
    private String industry;
    private double price;
    
    public Product(String id, String description, String industry, double price) {
        this.id = id;
        this.description = description;
        this.industry = industry;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getIndustry() {
        return industry;
    }

    public double getPrice() {
        return price;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    
    public String toString(){
        return "id="+id+": "+description+". "+industry+", $"+price;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Product other = (Product) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    

}
