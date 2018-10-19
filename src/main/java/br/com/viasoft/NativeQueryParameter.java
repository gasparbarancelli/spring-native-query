package br.com.viasoft;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class NativeQueryParameter {

    private String name;

    private Object value;

}
