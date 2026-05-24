package nips.dev.springqueryutils.support;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import nips.dev.springqueryutils.annotatons.FilterFieldAllies;
import nips.dev.springqueryutils.annotatons.SoftDeleteFlag;
import nips.dev.springqueryutils.annotatons.enums.SqlOperator;

@Entity
public class TestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @FilterFieldAllies(alias = "itemName", operator = SqlOperator.LIKE)
    private String name;

    @FilterFieldAllies(operator = SqlOperator.GREATER)
    private Integer score;

    @SoftDeleteFlag
    private boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
