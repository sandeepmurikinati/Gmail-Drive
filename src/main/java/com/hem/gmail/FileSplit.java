package com.hem.gmail;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileSplit {
    private Long len;
    private Long off;
}
