package aagapp_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "custom_role_table")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Role
{
@Id
    private int role_id;
    private String role_name;
    private String created_at,updated_at,created_by;
}
