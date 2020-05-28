package ch.ethz.semdwhsearch.prototyp1.pages.impl.phases;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.semdwhsearch.prototyp1.actions.Params;
import ch.ethz.semdwhsearch.prototyp1.classification.Classification;
import ch.ethz.semdwhsearch.prototyp1.constants.PageNames;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.FormElement;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.QueryElement;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.TitleElement;
import ch.ethz.semdwhsearch.prototyp1.pages.impl.AbstractPage;
import ch.ethz.semdwhsearch.prototyp1.tools.ErrorTools;
import ch.ethz.semdwhsearch.prototyp1.tools.Escape;
import ch.ethz.semdwhsearch.prototyp1.tools.request.PostRequest;

/**
 * A page for demo queries based on QALD4 biomedical benchmark
 * 
 * See https://github.com/ag-sc/QALD/blob/master/4/data/
 * @author Ana Sima
 * 
 */
public class DemoPageQald4 extends AbstractPage {
	private static final Logger logger = LoggerFactory.getLogger(DemoPageQald4.class);

	HashMap<String, String> classMap = new HashMap<String, String>();

	public String getInitJs() {
		return "document.getElementById(\"demo\").focus();";
	}
	
	public ArrayList<String> getQald4QuestionsTrain() {
		String Q1 = "What is the target drug of Vidarabine?";
		String Q2 = "Which are targets of Hydroxocobalamin?";
		String Q3 = "Which foods does allopurinol interact with?";
		String Q4 = "Which are the side effects of Penicillin G?";
		String Q5 = "Which diseases are associated with the gene FOXP2?";
		String Q6 = "Which genes are associated with breast cancer?";
		String Q7 = "Which are possible drugs for diseases associated with the gene ALD?";
		String Q8 = "Give me drug references of drugs targeting Prothrombin.";
		String Q9 = "Which drugs interact with allopurinol?";
		String Q10 = "Which are possible drugs against rickets?";
		String Q11 = "What are the side effects of Valdecoxib?";
		String Q12 = "Which genes are associated with diseases whose possible drugs target Cubilin?";
		String Q13 = "What are the common side effects of Doxil and Bextra?";
		String Q14 = "What are the diseases caused by Valdecoxib?";
		String Q15 = "What are side effects of drugs used for asthma?";
		String Q16 = "What is the side effects of drugs used for Tuberculosis?";
		String Q17 = "Which drugs have hypertension and vomiting as side effects?";
		String Q18 = "Which drugs target Multidrug resistance protein 1?";
		String Q19 = "Which diseases is Cetuximab used for?";
		String Q20 = "Which drugs have fever as a side effect?";
		String Q21 = "Give me diseases treated by tetracycline.";
		String Q22 = "What are enzymes of drugs used for anemia?";
		String Q23 = "Which genes are associated with diseases treated with Cetuximab?";
		String Q24 = "Which are targets for possible drugs for diseases associated with the gene ALD?";
		String Q25 = "Which are the drugs whose side effects are associated with the gene TRPM6?";
		
		ArrayList<String> results = new ArrayList<String>();
		results.add(Q1);results.add(Q2);results.add(Q3);results.add(Q4);results.add(Q5);
		results.add(Q6);results.add(Q7);results.add(Q8);results.add(Q9);results.add(Q10);
		results.add(Q11);results.add(Q12);results.add(Q13);results.add(Q14);results.add(Q15);
		results.add(Q16);results.add(Q17);results.add(Q18);results.add(Q19);results.add(Q20);
		results.add(Q21);results.add(Q22);results.add(Q23);results.add(Q24);results.add(Q25);
		
		return results;
	}
	
	public ArrayList<String> getQald4QuestionsTest() {
		String Q1 = "Which genes are associated with subtypes of rickets?";
		String Q2 = "Which drugs achieve a protein binding of 100%?";
		String Q3 = "Which drug has the highest number of side effects?";
		String Q4 = "Give me diseases whose possible drugs target the elongation factor 2.";
		String Q5 = "Which disease has the largest size?";
		String Q6 = "Which is the least common chromosome location?";
		String Q7 = "Which approved drugs interact with fibers?";
		String Q8 = "Which diseases are associated with SAR1B?";
		String Q9 = "List diseases whose possible drugs have no side effects.";
		String Q10 = "Which drugs have bipolar disorder as indication?";
		String Q11 = "Are there drugs that target the Protein kinase C beta type?";
		String Q12 = "Give me the drug categories of Desoxyn.";
		String Q13 = "Which diseases have a class degree of 11?";
		String Q14 = "Which experimental drugs interact with food?";
		String Q15 = "List drugs that lead to strokes and arthrosis.";
		String Q16 = "Give me all diseases of the connective tissue class.";
		String Q17 = "List the number of distinct side effects of drugs which target genes whose general function involves cell division.";
		String Q18 = "Which drugs have a water solubility of 2.78e-01 mg/mL?";
		String Q19 = "Which genes are associated with Endothelin receptor type B?";
		String Q20 = "Which drugs interact with food and have HIV infections as side effects?";
		String Q21 = "Which targets are involved in blood clotting?";
		String Q22 = "List illnesses that are treated by drugs whose mechanism of action involves norepinephrine and serotonin.";
		String Q23 = "Which drugs have no side effects?";
		String Q24 = "Give me the side effects of drugs with a solubility of 3.24e-02 mg/mL.";
		String Q25 = "Give me drugs in the gaseous state.";
		
		ArrayList<String> results = new ArrayList<String>();
		results.add(Q1);results.add(Q2);results.add(Q3);results.add(Q4);results.add(Q5);
		results.add(Q6);results.add(Q7);results.add(Q8);results.add(Q9);results.add(Q10);
		results.add(Q11);results.add(Q12);results.add(Q13);results.add(Q14);results.add(Q15);
		results.add(Q16);results.add(Q17);results.add(Q18);results.add(Q19);results.add(Q20);
		results.add(Q21);results.add(Q22);results.add(Q23);results.add(Q24);results.add(Q25);
		
		return results;
	}

	public String getHtml(HttpServletRequest req, Dictionary dict) {
		String qStr = null;
		if (req.getMethod().equals("GET")) {
			qStr = req.getParameter(Params.Q);
		} else if (req.getMethod().equals("POST")) {
			PostRequest postReq = new PostRequest();
			try {
				postReq.parse(req, null, false);
			} catch (Exception e) {
				return ErrorTools.toDivWarn("Could not parse post request!");
			}
			qStr = postReq.getFormField(Params.Q);
		}

		StringBuffer html = new StringBuffer();
		new TitleElement("QALD-4 Demo Page").appendHtml(html, dict);
		
		// form - use the main search page to answer free text queries
		FormElement form = new FormElement(PageNames.PN_SGRAPH_FEDERATED);
		form.appendHtml(html, dict);

		// query
		new QueryElement(qStr).appendHtml(html, dict);

		// close form
		form.appendHtmlClose(html, dict);

		html.append("<div class='content'>\n");

                html.append("<table class='questions'>\n");
                html.append("<tr>\n");
                html.append("<th>");
                html.append("Question Train");
                html.append("</th>");
                html.append("<th>");
                html.append("Question Test");
                html.append("</th>");
               
                ArrayList<String> trainQs = getQald4QuestionsTrain();
                ArrayList<String> testQs = getQald4QuestionsTest();
               
                for (int i = 0; i < trainQs.size(); i++) {                              
                        html.append("</tr>\n");
                        html.append("<tr>\n");
                       
                        html.append("<td>");
                        html.append("<a href=\"" + Escape.safeXml("http://biosoda.cloudlab.zhaw.ch:8083/soda/?page=biosoda&q=") + Escape.safeXml(trainQs.get(i)) + "\">");
                        html.append(trainQs.get(i));
                        html.append("</a>");
                        html.append("</td>\n");

                        html.append("<td>");
                        html.append("<a href=\"" + Escape.safeXml("http://biosoda.cloudlab.zhaw.ch:8083/soda/?page=biosoda&q=") + Escape.safeXml(testQs.get(i)).replace("%","%25") + "\">");
                        html.append(testQs.get(i));
                        html.append("</a>");
                        html.append("</td>\n");
                }

                html.append("</tr>\n");
                html.append("</table>\n");

                html.append("<ul>\n");

                html.append("<li>For details, see Official Source for QALD4 <a href=\"" + Escape.safeXml("https://github.com/ag-sc/QALD/blob/master/4/data/qald-4_biomedical_train_withanswers.xml")+ "\"> training </a> and " + "<a href=\""+ Escape.safeXml("https://github.com/ag-sc/QALD/blob/master/4/data/qald-4_biomedical_test_withanswers.xml") + "\"> testing </a> questions</li>");
                html.append("</ul>\n");
		
		logger.info("Final time: "+ new Timestamp(System.currentTimeMillis()));

		return html.toString();
	}
}
