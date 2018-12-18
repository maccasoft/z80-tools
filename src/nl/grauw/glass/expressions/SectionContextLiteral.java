package nl.grauw.glass.expressions;

public class SectionContextLiteral extends ContextLiteral {
	
	private final SectionContext sectionContext;
	
	public SectionContextLiteral(Context context, SectionContext sectionContext) {
		super(context);
		this.sectionContext = sectionContext;
	}
	
	@Override
	public boolean isSectionContext() {
		return true;
	}
	
	@Override
	public SectionContext getSectionContext() {
		return sectionContext;
	}
	
}
