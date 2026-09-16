package in.techgeneza.medycatalog.modules.catalog.application;

import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.StudentOption;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.StudentQuestion;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionOptionEntity;

import java.util.List;

public final class StudentQuestionMapper {

    private StudentQuestionMapper() {
    }

    public static StudentQuestion toStudent(QuestionEntity question, List<QuestionOptionEntity> options) {
        return new StudentQuestion(
                question.getId(),
                question.getQuestionText(),
                question.getQuestionType().getCode(),
                question.getQuestionType().isAllowsMultipleOptions(),
                question.getQuestionType().isRequiresNumerical(),
                question.getDifficulty(),
                question.getMarks(),
                question.getNegativeMarks(),
                question.getEstimatedTimeSeconds(),
                question.getTopicId(),
                options.stream()
                        .map(option -> new StudentOption(option.getId(), option.getOptionText(), option.getOptionOrder()))
                        .toList()
        );
    }
}
