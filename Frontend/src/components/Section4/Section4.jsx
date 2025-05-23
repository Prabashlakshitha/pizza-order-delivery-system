import React, { useEffect, useRef } from "react";
import { Container, Row, Col } from "react-bootstrap";
import "./Section4.css";

const Section4 = () => {
  const h4Ref = useRef(null);
  const h2Ref = useRef(null);
  const pRef = useRef(null);
  const buttonRef = useRef(null);

  const isInViewport = (element) => {
    const rect = element.getBoundingClientRect();
    return rect.top >= 0 && rect.bottom <= window.innerHeight;
  };

  const resetAnimationStyles = (element) => {
    element.style.opacity = 0;
    element.style.transform = "translateY(50px)";
  };

  const onScroll = () => {
    const elements = [h4Ref, h2Ref, pRef, buttonRef];

    elements.forEach((ref, index) => {
      if (ref.current) {
        const element = ref.current;

        if (isInViewport(element)) {
          element.style.transition = "transform 1s ease, opacity 1s ease";
          element.style.opacity = 1;
          element.style.transform = "translateY(0)";
          element.style.transitionDelay = `${0.2 * (index + 1)}s`;
        } else {
          resetAnimationStyles(element);
        }
      }
    });
  };

  useEffect(() => {
    window.addEventListener("scroll", onScroll);
    window.addEventListener("load", onScroll);

    return () => {
      window.removeEventListener("scroll", onScroll);
      window.removeEventListener("load", onScroll);
    };
  }, []);

  useEffect(() => {
    const elements = [h4Ref, h2Ref, pRef, buttonRef];
    elements.forEach((ref) => {
      if (ref.current) {
        resetAnimationStyles(ref.current);
      }
    });
  }, []);

  return (
    <div>
      <section className="contact_section">
        <Container>
          <Row className="justify-content-center">
            <Col sm={8} className="text-center">
              <h4 ref={h4Ref}>We Guarantee</h4>
              <h2 ref={h2Ref}>30 Minutes Delivery!</h2>
              <p ref={pRef}>
                We ensure that your order will reach you within 30 minutes, so
                you can enjoy your pizza at its best—hot, fresh, and ready to eat.
              </p>
              <button ref={buttonRef} className="call-now">
                Call: 999-888-7777
              </button>
            </Col>
          </Row>
        </Container>
      </section>
    </div>
  );
};

export default Section4;
