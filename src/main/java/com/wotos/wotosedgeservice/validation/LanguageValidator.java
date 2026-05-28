package com.wotos.wotosedgeservice.validation;

import com.wotos.wotosedgeservice.validation.constraints.Language;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.EnumSet;

public class LanguageValidator implements ConstraintValidator<Language, String> {

    public enum Language {
        ENGLISH("English", "en"),
        RUSSIAN("Русский", "ru"),
        POLISH("Polski", "pl"),
        GERMAN("Deutsch", "de"),
        FRENCH("Français", "fr"),
        SPANISH("Español", "es"),
        CHINESE_SIMPLIFIED_CHINA("简体中文", "zh-cn"),
        CHINESE_TRADITIONAL_TAIWAN("繁體中文", "zh-tw"),
        TURKISH("Türkçe", "tr"),
        CZECH("Čeština", "cs"),
        THAI("ไทย", "th"),
        VIETNAMESE("Tiếng Việt", "vi"),
        KOREAN("한국어", "ko");

        private final String name;
        private final String code;

        Language(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }

    @Override
    public boolean isValid(String code, ConstraintValidatorContext constraintValidatorContext) {
        Language language = EnumSet.allOf(Language.class).stream().filter(value -> value.code.equalsIgnoreCase(code)).findFirst().orElse(null);

        if (language != null) {
            return true;
        }

        constraintValidatorContext.buildConstraintViolationWithTemplate("Language Code: " + code + " is invalid").addConstraintViolation();
        return false;
    }
}
