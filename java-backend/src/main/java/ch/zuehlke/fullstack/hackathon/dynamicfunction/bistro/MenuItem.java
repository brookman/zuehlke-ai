package ch.zuehlke.fullstack.hackathon.dynamicfunction.bistro;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class MenuItem {
    private String week_day;
    private String menu_type;
    private String name;
    private List<String> ingredients;
}

