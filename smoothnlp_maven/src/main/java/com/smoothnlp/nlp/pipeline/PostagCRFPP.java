package com.smoothnlp.nlp.pipeline;

import com.smoothnlp.nlp.SmoothNLP;
import com.smoothnlp.nlp.basic.SToken;
import com.smoothnlp.nlp.basic.UtilFns;
import com.smoothnlp.nlp.model.crfpp.ModelImpl;
import com.smoothnlp.nlp.model.crfpp.Tagger;

import java.util.ArrayList;
import java.util.List;

public class PostagCRFPP extends CRFModel{
    protected ModelImpl model;
    protected ISequenceTagger segment_pipeline;
    private List<String> libraryNames = null;


    public PostagCRFPP(){
        this.model = new ModelImpl();
        this.model.open(SmoothNLP.CRF_POSTAG_MODEL,0,0,1.0);
    }

    public PostagCRFPP(ISequenceTagger segment_pipeline){
        this.segment_pipeline = segment_pipeline;
    }

    public void setActiveDictionaries(List<String> libraryNames){
        this.libraryNames = libraryNames;
    }

    public void setSegment_pipeline(ISequenceTagger segment_pipeline) {
        this.segment_pipeline = segment_pipeline;
    }

    public List<SToken> process(String input){
        if (segment_pipeline==null){
            segment_pipeline = new SegmentCRFPP();
        }
        List<SToken> stokens = segment_pipeline.process(input);
        return process(stokens);
    };

    public List<SToken> process(List<SToken> stokens){
        Tagger tagger = this.model.createTagger();
        if (tagger==null){
            SmoothNLP.LOGGER.severe(String.format("CRF segment model is not properly read"));
        }
        if (stokens == null || stokens.size() == 0) {
            return new ArrayList<SToken>();
        }else{
            for (SToken stoken: stokens){
                String ftr = super.buildFtrs(stoken.getToken());
                tagger.add(ftr);
            }
            tagger.parse();
            for (int i=0; i<stokens.size();i++){
                String ytag = tagger.yname(tagger.y(i));  // predict的t
                stokens.get(i).setPostag(ytag);
            }
        }
        return stokens;
    };

    public static void main(String[] args){
        ISequenceTagger s = new PostagCRFPP();
        System.out.println(s.process("五十块钱买了两个冰淇淋还是挺便宜的"));
        System.out.println(UtilFns.toJson(s.process("广汽集团上月利润达到5万")));
    }

}
