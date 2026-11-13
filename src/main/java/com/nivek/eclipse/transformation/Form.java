package com.nivek.eclipse.transformation;

/** Formas de transformacao. O indice (ordinal) e o valor guardado no Attachment ACTIVE_FORM (-1 = nenhuma). */
public enum Form {
    TENTACLES("Tentaculos", "+4 de dano e +1,5 de alcance", 3, 6),
    ARMOR("Armadura Eclipse", "+8 armadura, +4 resistencia, +0,5 repulsao", 4, 6),
    WINGS("Asas", "+15% velocidade, queda lenta e queda segura", 4, 5),
    ECLIPSE("Forma Eclipse", "Todos os bonus, mais fortes", 5, 8);

    public final String label;
    public final String description;
    /** Nivel de mutacao minimo. */
    public final int requiredMutation;
    /** Energia gasta por segundo enquanto a forma esta ativa. */
    public final int upkeep;

    Form(String label, String description, int requiredMutation, int upkeep) {
        this.label = label;
        this.description = description;
        this.requiredMutation = requiredMutation;
        this.upkeep = upkeep;
    }
}
